package test.cmp.auth.entity

import java.util.UUID

internal class EncryptedKey(
    val id: UUID,
    val cs: CipherSpec,
    val encoded: ByteArray,
)
