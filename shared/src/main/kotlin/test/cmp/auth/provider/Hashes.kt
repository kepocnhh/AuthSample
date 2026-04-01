package test.cmp.auth.provider

internal interface Hashes {
    fun sha256(encoded: ByteArray): ByteArray
}
