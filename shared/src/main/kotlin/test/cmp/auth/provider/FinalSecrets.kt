package test.cmp.auth.provider

import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.interfaces.ECPrivateKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPrivateKeySpec
import java.security.spec.ECPublicKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.jce.ECNamedCurveTable

internal class FinalSecrets : Secrets {
    override fun toPrivateKey(encoded: ByteArray): PrivateKey {
        val kf = KeyFactory.getInstance("ec")
        return kf.generatePrivate(PKCS8EncodedKeySpec(encoded))
    }

    override fun toPublicKey(encoded: ByteArray): PublicKey {
        val kf = KeyFactory.getInstance("ec")
        return kf.generatePublic(X509EncodedKeySpec(encoded))
    }

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

    override fun getSecretKey(password: String, salt: ByteArray, iterations: Int, keySize: Int): SecretKey {
        val keyFactory = SecretKeyFactory.getInstance("pbkdf2withhmacsha$keySize")
        val keySpec = PBEKeySpec(password.toCharArray(), salt, iterations, keySize)
        return SecretKeySpec(keyFactory.generateSecret(keySpec).encoded, "aes")
    }

    override fun newKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("ec")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        return kpg.generateKeyPair()
    }

    override fun getSharedKey(thisKey: PrivateKey, thatKey: PublicKey): SecretKey {
        val ka = KeyAgreement.getInstance("ecdh")
        ka.init(thisKey)
        ka.doPhase(thatKey, true)
        val md = MessageDigest.getInstance("sha256")
        return SecretKeySpec(md.digest(ka.generateSecret()), "aes")
    }

    override fun encrypt(key: SecretKey, decrypted: ByteArray, spec: GCMParameterSpec): ByteArray {
        val cipher = Cipher.getInstance("aes/gcm/nopadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)
        return cipher.doFinal(decrypted)
    }

    override fun decrypt(key: SecretKey, encrypted: ByteArray, spec: GCMParameterSpec): ByteArray {
        val cipher = Cipher.getInstance("aes/gcm/nopadding")
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(encrypted)
    }

    override fun nextBytes(bytes: ByteArray) {
        val random = SecureRandom.getInstanceStrong()
        random.nextBytes(bytes)
    }

    override fun sign(key: PrivateKey, encoded: ByteArray): ByteArray {
        val sig = Signature.getInstance("sha256withecdsa")
        sig.initSign(key)
        sig.update(encoded)
        return sig.sign()
    }

    override fun verify(key: PublicKey, signature: ByteArray, encoded: ByteArray): Boolean {
        val sig = Signature.getInstance("sha256withecdsa")
        sig.initVerify(key)
        sig.update(encoded)
        return sig.verify(signature)
    }
}
