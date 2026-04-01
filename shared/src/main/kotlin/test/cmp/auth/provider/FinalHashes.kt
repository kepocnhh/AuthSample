package test.cmp.auth.provider

import java.security.MessageDigest

internal class FinalHashes : Hashes {
    override fun sha256(encoded: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("sha256")
        return md.digest(encoded)
    }
}
