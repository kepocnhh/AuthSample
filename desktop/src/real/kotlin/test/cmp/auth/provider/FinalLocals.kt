package test.cmp.auth.provider

import java.security.PrivateKey
import test.cmp.auth.entity.EncryptedKey

internal class FinalLocals : Locals {
    override var ek: EncryptedKey? = null
    override var pk: PrivateKey? = null
}
