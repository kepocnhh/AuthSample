package test.cmp.auth.provider

import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

internal interface Secrets {
    fun getSeed(passphrase: String): ByteArray
    fun getMasterKey(seed: ByteArray): SecretKey
    fun getPrivateKey(key: SecretKey): PrivateKey
    fun getPublicKey(key: PrivateKey): PublicKey
}
