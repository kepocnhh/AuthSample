package test.cmp.auth.provider

import test.cmp.auth.entity.EncryptedKey
import java.security.PrivateKey

internal class FinalLocals(
    private val dirs: Dirs,
) : Locals {
    override var ek: EncryptedKey?
        get() {
            val file = dirs.files.resolve("key")
            if (!file.exists()) return null
            return EncryptedKey(encoded = file.readBytes())
        }
        set(value) {
            val file = dirs.files.resolve("key")
            if (value == null) {
                file.delete()
            } else {
                file.writeBytes(value.encoded)
            }
        }

    override var pk: PrivateKey? = null
}
