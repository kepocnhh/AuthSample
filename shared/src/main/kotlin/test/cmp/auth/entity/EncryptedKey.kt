package test.cmp.auth.entity

import java.security.PublicKey

internal class EncryptedKey(
    val cs: CipherSpec,
    val encoded: ByteArray,
    val pub: PublicKey,
)
