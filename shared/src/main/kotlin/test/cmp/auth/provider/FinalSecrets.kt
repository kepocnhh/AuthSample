package test.cmp.auth.provider

import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.interfaces.ECPrivateKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPrivateKeySpec
import java.security.spec.ECPublicKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.jce.ECNamedCurveTable

internal class FinalSecrets : Secrets {
    override fun getSeed(passphrase: String): ByteArray {
        val md = MessageDigest.getInstance("sha256")
        md.update("seed".toByteArray())
        md.update(passphrase.toByteArray())
        return md.digest()
    }

    override fun getMasterKey(seed: ByteArray): SecretKey {
        val md = MessageDigest.getInstance("sha512")
        md.update("masterkey".toByteArray())
        md.update(seed)
        return SecretKeySpec(md.digest(), "aes")
    }

    override fun getPrivateKey(key: SecretKey): PrivateKey {
        val ap = AlgorithmParameters.getInstance("ec")
        ap.init(ECGenParameterSpec("secp256r1"))
        val spec = ap.getParameterSpec(ECParameterSpec::class.java)
        val magnitude = key.encoded.copyOfRange(fromIndex = 0, toIndex = 32)
        val s = BigInteger(1, magnitude).mod(spec.order)
        val kf = KeyFactory.getInstance("ec")
        return kf.generatePrivate(ECPrivateKeySpec(s, spec))
    }

    override fun getPublicKey(key: PrivateKey): PublicKey {
        check(key is ECPrivateKey)
        val curve = ECNamedCurveTable.getParameterSpec("secp256r1").curve
        val point = curve
            .multiplier
            .multiply(curve.createPoint(key.params.generator.affineX, key.params.generator.affineY), key.s)
            .normalize()
        val w = ECPoint(point.affineXCoord.toBigInteger(), point.affineYCoord.toBigInteger())
        val kf = KeyFactory.getInstance("ec")
        return kf.generatePublic(ECPublicKeySpec(w, key.params))
    }

    override fun encrypt(key: PublicKey, decrypted: ByteArray): ByteArray {
        val kpg = KeyPairGenerator.getInstance("ec")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        val kp = kpg.generateKeyPair()
        val ka = KeyAgreement.getInstance("ecdh")
        ka.init(kp.private)
        ka.doPhase(key, true)
        val md = MessageDigest.getInstance("sha256")
        val sk = SecretKeySpec(md.digest(ka.generateSecret()), "aes")
        val cipher = Cipher.getInstance("aes/gcm/nopadding")
        val nonce = ByteArray(12)
        val random = SecureRandom.getInstanceStrong()
        random.nextBytes(nonce)
        cipher.init(Cipher.ENCRYPT_MODE, sk, GCMParameterSpec(128, nonce))
        return cipher.doFinal(decrypted)
    }

    override fun decrypt(key: PrivateKey, encrypted: ByteArray): ByteArray {
        TODO("Secrets:decrypt")
    }
}
