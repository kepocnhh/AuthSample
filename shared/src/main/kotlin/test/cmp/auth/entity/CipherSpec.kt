package test.cmp.auth.entity

import javax.crypto.spec.GCMParameterSpec

internal class CipherSpec(
    val iterations: Int,
    val salt: ByteArray,
    val spec: GCMParameterSpec,
)
