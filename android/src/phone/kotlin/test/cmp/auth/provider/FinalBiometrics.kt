package test.cmp.auth.provider

import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import test.cmp.auth.BuildConfig
import java.security.KeyStore
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.coroutines.suspendCoroutine

internal class FinalBiometrics(
    private val context: Context,
) : Biometrics<GCMParameterSpec> {
    private val keyAlias = BuildConfig.APPLICATION_ID
    private val algorithm = KeyProperties.KEY_ALGORITHM_AES
    private val blocks = KeyProperties.BLOCK_MODE_GCM
    private val paddings = KeyProperties.ENCRYPTION_PADDING_NONE
    private val keySize = 256
    private val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    private val executor = Executors.newSingleThreadExecutor()

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (keyStore.containsAlias(keyAlias)) return keyStore.getKey(keyAlias, null) as SecretKey
        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val spec = KeyGenParameterSpec
            .Builder(keyAlias, purposes)
            .setBlockModes(blocks)
            .setEncryptionPaddings(paddings)
            .setKeySize(keySize)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)
            .build()
        val keyGenerator = KeyGenerator.getInstance(algorithm, keyStore.provider)
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private suspend fun BiometricPrompt.getCipher(issuer: Cipher): Cipher {
        return suspendCoroutine { continuation ->
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    continuation.resumeWith(Result.failure(IllegalStateException())) // todo
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val result = runCatching {
                        result.cryptoObject?.cipher ?: error("No cipher!")
                    }
                    continuation.resumeWith(result)
                }
            }
            val co = BiometricPrompt.CryptoObject(issuer)
            val cs = CancellationSignal()
            authenticate(co, cs, executor, callback)
        }
    }

    override suspend fun encrypt(decrypted: ByteArray): Pair<GCMParameterSpec, ByteArray> {
        val issuer = Cipher.getInstance("$algorithm/$blocks/$paddings")
        issuer.init(Cipher.ENCRYPT_MODE, getKey())
        val cipher = BiometricPrompt.Builder(context)
            .setTitle("encrypt")
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(true)
            .build()
            .getCipher(issuer = issuer)
        val encrypted = cipher.doFinal(decrypted)
        val spec = cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
        return Pair(spec, encrypted)
    }

    override suspend fun decrypt(
        encrypted: ByteArray,
        spec: GCMParameterSpec,
    ): ByteArray {
        val issuer = Cipher.getInstance("$algorithm/$blocks/$paddings")
        issuer.init(Cipher.DECRYPT_MODE, getKey(), spec)
        return BiometricPrompt.Builder(context)
            .setTitle("encrypt")
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(true)
            .build()
            .getCipher(issuer = issuer)
            .doFinal(encrypted)
    }
}
