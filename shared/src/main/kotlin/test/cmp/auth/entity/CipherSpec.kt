package test.cmp.auth.entity

internal class CipherSpec(
    val iterations: Int,
    val salt: ByteArray,
    val nonce: ByteArray,
)
