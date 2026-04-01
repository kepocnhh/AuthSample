package test.cmp.auth.provider

import test.cmp.auth.entity.EncryptedKey

internal interface Transformers {
    val ek: Transformer<EncryptedKey>
}

internal interface Transformer<T : Any> {
    fun encode(decoded: T): ByteArray
    fun decode(encoded: ByteArray): T
}
