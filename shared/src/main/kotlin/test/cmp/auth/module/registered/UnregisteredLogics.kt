package test.cmp.auth.module.registered

import kotlinx.coroutines.withContext
import sp.kx.logics.Logics
import test.cmp.auth.provider.Providers

internal class UnregisteredLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    private val logger = providers.loggers.create("[Unregistered]")

    fun register(passphrase: String, password: String) = launch {
        withContext(providers.contexts.default) {
            val seed = providers.secrets.getSeed(passphrase = passphrase)
            val mk = providers.secrets.getMasterKey(seed = seed)
            val pk = providers.secrets.getPrivateKey(key = mk)
            
            val pub = providers.secrets.getPublicKey(key = pk)
            val expected = "foo bar baz ${System.currentTimeMillis()}"
            val ekp = providers.secrets.newKeyPair()
            val thisKey = providers.secrets.getSharedKey(thisKey = ekp.private, thatKey = pub)
            val nonce = ByteArray(12)
            providers.secrets.nextBytes(nonce)
            val encrypted = providers.secrets.encrypt(key = thisKey, decrypted = expected.toByteArray(), nonce = nonce)
            val thatKey = providers.secrets.getSharedKey(thisKey = pk, thatKey = ekp.public)
            val decrypted = providers.secrets.decrypt(key = thatKey, encrypted = encrypted, nonce = nonce)
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
