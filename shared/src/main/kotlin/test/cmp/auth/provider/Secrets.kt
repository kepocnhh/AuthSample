package test.cmp.auth.provider

import javax.crypto.SecretKey

internal interface Secrets {
    fun getSeed(passphrase: String): ByteArray
    fun getMasterKey(seed: ByteArray): SecretKey
}
