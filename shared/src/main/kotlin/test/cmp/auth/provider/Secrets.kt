package test.cmp.auth.provider

import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

internal interface Secrets {
    fun getSeed(passphrase: String): ByteArray
    fun getMasterKey(seed: ByteArray): SecretKey
    fun getPrivateKey(key: SecretKey): PrivateKey
    fun getPublicKey(key: PrivateKey): PublicKey
    fun getSecretKey(password: String, salt: ByteArray, iterations: Int, keyLength: Int): SecretKey
    fun newKeyPair(): KeyPair
    fun getSharedKey(thisKey: PrivateKey, thatKey: PublicKey): SecretKey
    fun encrypt(key: SecretKey, decrypted: ByteArray, nonce: ByteArray): ByteArray
    fun decrypt(key: SecretKey, encrypted: ByteArray, nonce: ByteArray): ByteArray
    fun nextBytes(bytes: ByteArray)
}
