package test.cmp.auth.util

import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import test.cmp.auth.BuildConfig
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

internal object Biometrics {
    private val keyAlias = BuildConfig.APPLICATION_ID

    private const val algorithm = KeyProperties.KEY_ALGORITHM_AES
    private const val blocks = KeyProperties.BLOCK_MODE_GCM
    private const val paddings = KeyProperties.ENCRYPTION_PADDING_NONE
    private const val keyLength = 256
    private const val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
    private val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            TODO()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            TODO()
        }
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (keyStore.containsAlias(keyAlias)) return keyStore.getKey(keyAlias, null) as SecretKey
        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val spec = KeyGenParameterSpec
            .Builder(keyAlias, purposes)
            .setBlockModes(blocks)
            .setEncryptionPaddings(paddings)
            .setKeySize(keyLength)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            .build()
        val keyGenerator = KeyGenerator.getInstance(algorithm, keyStore.provider)
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun authenticate(
        context: Context,
        title: CharSequence,
    ) {
        val cipher = Cipher.getInstance("$algorithm/$blocks/$paddings")
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        BiometricPrompt.Builder(context)
            .setTitle(title)
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(true)
            .build()
            .authenticate(BiometricPrompt.CryptoObject(cipher), CancellationSignal(), context.mainExecutor, callback)
    }
}
