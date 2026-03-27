package test.cmp.auth.provider

import java.security.MessageDigest
import java.security.spec.KeySpec
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal class FinalSecrets : Secrets {
    override fun getSeed(passphrase: String): ByteArray {
        val skf = SecretKeyFactory.getInstance("pbkdf2withhmacsha512")
        val md = MessageDigest.getInstance("sha256")
        md.update("seed".toByteArray())
        md.update(passphrase.toByteArray())
        val salt = md.digest()
        val iterations = 2048
        val keyLength = 512
        val keySpec: KeySpec = PBEKeySpec(passphrase.toCharArray(), salt, iterations, keyLength)
        return skf.generateSecret(keySpec).encoded
    }

    override fun getMasterKey(seed: ByteArray): SecretKey {
        val mac = Mac.getInstance("hmacsha512")
        val md = MessageDigest.getInstance("sha256")
        md.update("masterkey".toByteArray())
        md.update(seed)
        val key = SecretKeySpec(md.digest(), mac.algorithm)
        mac.init(key)
        val encoded = mac.doFinal(seed)
        return SecretKeySpec(encoded, "aes")
    }
}
