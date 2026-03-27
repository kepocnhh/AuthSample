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
            val key = providers.secrets.getMasterKey(seed = seed)
            logger.debug("masterkey(${key.algorithm}/${key.encoded.size}): ${key.encoded.toList()}")
            TODO()
        }
    }
}
