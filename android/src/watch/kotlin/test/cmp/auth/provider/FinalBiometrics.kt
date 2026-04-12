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
    private val authenticators = BiometricManager.Authenticators.DEVICE_CREDENTIAL
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
            .setUserAuthenticationParameters(1, KeyProperties.AUTH_DEVICE_CREDENTIAL)
            .build()
        val keyGenerator = KeyGenerator.getInstance(algorithm, keyStore.provider)
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private suspend fun BiometricPrompt.authenticate() {
        return suspendCoroutine { continuation ->
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    continuation.resumeWith(Result.failure(IllegalStateException())) // todo
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    continuation.resumeWith(Result.success(Unit))
                }
            }
            val cs = CancellationSignal()
            authenticate(cs, executor, callback)
        }
    }

    override suspend fun encrypt(decrypted: ByteArray): Pair<GCMParameterSpec, ByteArray> {
        val cipher = Cipher.getInstance("$algorithm/$blocks/$paddings")
        BiometricPrompt.Builder(context)
            .setTitle("encrypt")
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(true)
            .build()
            .authenticate()
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val encrypted = cipher.doFinal(decrypted)
        val spec = cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
        return Pair(spec, encrypted)
    }

    override suspend fun decrypt(
        encrypted: ByteArray,
        spec: GCMParameterSpec,
    ): ByteArray {
        val cipher = Cipher.getInstance("$algorithm/$blocks/$paddings")
        BiometricPrompt.Builder(context)
            .setTitle("encrypt")
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(true)
            .build()
            .authenticate()
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        return cipher.doFinal(encrypted)
    }
}
