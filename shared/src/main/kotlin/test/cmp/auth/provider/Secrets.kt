package test.cmp.auth.provider

import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal interface Secrets {
    fun toPrivateKey(encoded: ByteArray): PrivateKey
    fun toPublicKey(encoded: ByteArray): PublicKey
    fun getSeed(passphrase: String): ByteArray
    fun getMasterKey(seed: ByteArray): SecretKey
    fun getPrivateKey(key: SecretKey): PrivateKey
    fun getPublicKey(key: PrivateKey): PublicKey
    fun getSecretKey(password: String, salt: ByteArray, iterations: Int, keySize: Int): SecretKey
    fun newKeyPair(): KeyPair
    fun getSharedKey(thisKey: PrivateKey, thatKey: PublicKey): SecretKey
    fun encrypt(key: SecretKey, decrypted: ByteArray, spec: GCMParameterSpec): ByteArray
    fun decrypt(key: SecretKey, encrypted: ByteArray, spec: GCMParameterSpec): ByteArray
    fun nextBytes(bytes: ByteArray)
    fun sign(key: PrivateKey, encoded: ByteArray): ByteArray
    fun verify(key: PublicKey, signature: ByteArray, encoded: ByteArray): Boolean
}
