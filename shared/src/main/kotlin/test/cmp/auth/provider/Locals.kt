package test.cmp.auth.provider

import java.security.PrivateKey
import test.cmp.auth.entity.EncryptedKey

internal interface Locals {
    var ek: EncryptedKey?
    var pk: PrivateKey?
}
