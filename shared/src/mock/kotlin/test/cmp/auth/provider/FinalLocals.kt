package test.cmp.auth.provider

import java.security.PrivateKey
import test.cmp.auth.Mocks
import test.cmp.auth.entity.EncryptedKey

internal class FinalLocals(dirs: Dirs) : Locals {
    override var ek: EncryptedKey? = Mocks.initialEncryptedKey
    override var pk: PrivateKey? = Mocks.initialPrivateKey
}
