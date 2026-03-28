package test.cmp.auth.module.registered

import kotlinx.coroutines.withContext
import sp.kx.logics.Logics
import test.cmp.auth.provider.Providers

internal class UnregisteredLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    private val logger = providers.loggers.create("[Unregistered]")

    fun register(passphrase: String) = launch {
        withContext(providers.contexts.default) {
            val seed = providers.secrets.getSeed(passphrase = passphrase)
            val mk = providers.secrets.getMasterKey(seed = seed)
            val pk = providers.secrets.getPrivateKey(key = mk)
            val pub = providers.secrets.getPublicKey(key = pk)
            val expected = "foo bar baz ${System.currentTimeMillis()}"
            val encrypted = providers.secrets.encrypt(key = pub, decrypted = expected.toByteArray())
            val decrypted = providers.secrets.decrypt(key = pk, encrypted = encrypted)
            val actual = String(decrypted)
            val message = """
                expected(${expected.length}): "$expected"
                actual(${actual.length}): "$actual"
            """.trimIndent()
            logger.debug(message)
            TODO("UnregisteredLogics:register")
        }
    }
}
