package test.cmp.auth.provider

import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPrivateKeySpec

internal class FinalSecrets : Secrets {
    override fun getSeed(passphrase: String): ByteArray {
        val md = MessageDigest.getInstance("sha256")
        md.update("seed".toByteArray())
        md.update(passphrase.toByteArray())
        return md.digest()
    }

    override fun getMasterKey(seed: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("sha512")
        md.update("masterkey".toByteArray())
        md.update(seed)
        return md.digest()
    }

    override fun getPrivateKey(masterKey: ByteArray): PrivateKey {
        val secretKey = masterKey.copyOfRange(fromIndex = 0, toIndex = 32)
        val ap = AlgorithmParameters.getInstance("ec")
        ap.init(ECGenParameterSpec("secp256r1"))
        val spec = ap.getParameterSpec(ECParameterSpec::class.java)
        val number = BigInteger(1, secretKey).mod(spec.order)
        val kf = KeyFactory.getInstance("ec")
        return kf.generatePrivate(ECPrivateKeySpec(number, spec))
    }
}
