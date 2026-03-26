package test.cmp.auth.provider

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import test.cmp.auth.BuildConfig
import test.cmp.auth.entity.EncryptedKey
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

internal class FinalLocals(
    private val dirs: Dirs,
) : Locals {
    private fun getSpec(
        alias: String,
        blocks: String,
        paddings: String,
        keySize: Int,
    ): AlgorithmParameterSpec {
        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        return KeyGenParameterSpec
            .Builder(alias, purposes)
            .setBlockModes(blocks)
            .setEncryptionPaddings(paddings)
            .setKeySize(keySize)
            .build()
    }

    private fun getKey(
        alias: String,
        blocks: String,
        paddings: String,
    ): SecretKey = synchronized(Unit) {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        val key = ks.getKey(alias, null)
        if (key == null) {
            val kg = KeyGenerator.getInstance("aes", "AndroidKeyStore")
            val spec = getSpec(
                alias = alias,
                blocks = blocks,
                paddings = paddings,
                keySize = 256,
            )
            kg.init(spec)
            return kg.generateKey()
        }
        check(key is SecretKey)
        return key
    }

    private var _bytes: ByteArray? = null

    override var key: EncryptedKey?
        get() {
//            val file = File(dirs.files, "key")
//            if (!file.exists()) return null
            val bytes = _bytes ?: return null
            val blocks = KeyProperties.BLOCK_MODE_GCM
            val paddings = KeyProperties.ENCRYPTION_PADDING_NONE
            val key = getKey(
                alias = BuildConfig.APPLICATION_ID,
                blocks = blocks,
                paddings = paddings,
            )
            val cipher = Cipher.getInstance("aes/$blocks/$paddings")
            val tLen: Int
            val iv = ByteArray(12)
            val encrypted = ByteArray(bytes.size - 1 - iv.size)
            ByteArrayInputStream(bytes).use { stream ->
                tLen = stream.read()
                stream.read(iv)
                stream.read(encrypted)
            }
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(tLen, iv))
//            val encoded = cipher.doFinal(file.readBytes())
            val encoded = cipher.doFinal(encrypted)
            return EncryptedKey(encoded = encoded)
        }
        set(value) {
//            val file = File(dirs.files, "key")
            if (value == null) {
//                file.delete()
                _bytes = null
            } else {
                val blocks = KeyProperties.BLOCK_MODE_GCM
                val paddings = KeyProperties.ENCRYPTION_PADDING_NONE
                val key = getKey(
                    alias = BuildConfig.APPLICATION_ID,
                    blocks = blocks,
                    paddings = paddings,
                )
                val cipher = Cipher.getInstance("aes/$blocks/$paddings")
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val spec = cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
                println("tLen: ${spec.tLen}")
                println("iv:size: ${spec.iv.size}")
                val encrypted = cipher.doFinal(value.encoded)
                _bytes = ByteArrayOutputStream().use { stream ->
                    stream.write(spec.tLen)
                    stream.write(spec.iv)
                    stream.write(encrypted)
                    stream.toByteArray()
                }
            }
        }
}
