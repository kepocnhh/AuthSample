package test.cmp.auth.provider

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import sp.kx.bytes.readBytes
import sp.kx.bytes.readInt
import sp.kx.bytes.writeBytes
import test.cmp.auth.entity.CipherSpec
import test.cmp.auth.entity.EncryptedKey

internal class FinalTransformers(
    private val hashes: Hashes,
    private val secrets: Secrets,
) : Transformers {
    override val ek = object : Transformer<EncryptedKey> {
        override fun encode(decoded: EncryptedKey): ByteArray {
            return ByteArrayOutputStream().use { stream ->
                stream.writeBytes(decoded.cs.iterations)
                stream.writeBytes(decoded.cs.salt.size)
                stream.writeBytes(decoded.cs.salt)
                stream.writeBytes(decoded.cs.nonce.size)
                stream.writeBytes(decoded.cs.nonce)
                stream.writeBytes(decoded.encoded.size)
                stream.writeBytes(decoded.encoded)
                stream.writeBytes(decoded.pub.encoded.size)
                stream.writeBytes(decoded.pub.encoded)
                val hash = hashes.sha256(stream.toByteArray())
                stream.writeBytes(hash.size)
                stream.writeBytes(hash)
                stream.toByteArray()
            }
        }

        override fun decode(encoded: ByteArray): EncryptedKey {
            return ByteArrayInputStream(encoded).use { src ->
                ByteArrayOutputStream().use { dst ->
                    val iterations = src.readBytes(4)
                        .also(dst::writeBytes)
                        .readInt()
                    val salt = src.readBytes(4)
                        .also(dst::writeBytes)
                        .readInt()
                        .let(src::readBytes)
                        .also(dst::writeBytes)
                    val nonce = src.readBytes(4)
                        .also(dst::writeBytes)
                        .readInt()
                        .let(src::readBytes)
                        .also(dst::writeBytes)
                    val encoded = src.readBytes(4)
                        .also(dst::writeBytes)
                        .readInt()
                        .let(src::readBytes)
                        .also(dst::writeBytes)
                    val pub = src.readBytes(4)
                        .also(dst::writeBytes)
                        .readInt()
                        .let(src::readBytes)
                        .also(dst::writeBytes)
                        .let(secrets::toPublicKey)
                    val hash = src.readBytes(4)
                        .also(dst::writeBytes)
                        .readInt()
                        .let(src::readBytes)
                    if (!hash.contentEquals(hashes.sha256(dst.toByteArray()))) TODO("Transformers:ek:decode")
                    EncryptedKey(
                        cs = CipherSpec(
                            iterations = iterations,
                            salt = salt,
                            nonce = nonce,
                        ),
                        encoded = encoded,
                        pub = pub,
                    )
                }
            }
        }
    }
}
