package test.cmp.auth.provider

import test.cmp.auth.entity.EncryptedKey
import java.security.PrivateKey

internal class FinalLocals(
    private val dirs: Dirs,
    private val transformers: Transformers,
    loggers: Loggers,
) : Locals {
    private val logger = loggers.create("[Locals]")

    override var ek: EncryptedKey?
        get() {
            val file = dirs.keys.resolve("ek")
            if (!file.exists()) return null
            return try {
                check(file.isFile)
                transformers.ek.decode(file.readBytes())
            } catch (error: Throwable) {
                logger.warning("read file ${file.name} error: $error")
                return null
            }
        }
        set(value) {
            val file = dirs.keys.resolve("ek")
            if (value == null) {
                if (file.exists()) {
                    check(file.isFile)
                    check(file.delete())
                }
            } else {
                file.writeBytes(transformers.ek.encode(decoded = value))
            }
        }

    override var pk: PrivateKey? = null
}
