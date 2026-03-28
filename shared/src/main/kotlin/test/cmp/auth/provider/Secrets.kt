package test.cmp.auth.provider

import java.security.PrivateKey

internal interface Secrets {
    fun getSeed(passphrase: String): ByteArray
    fun getMasterKey(seed: ByteArray): ByteArray
    fun getPrivateKey(masterKey: ByteArray): PrivateKey
}
