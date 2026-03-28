package test.cmp.auth.module.registered

import kotlinx.coroutines.withContext
import sp.kx.logics.Logics
import test.cmp.auth.provider.Providers

internal class UnregisteredLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    fun register(passphrase: String) = launch {
        withContext(providers.contexts.default) {
            val seed = providers.secrets.getSeed(passphrase = passphrase)
            val mk = providers.secrets.getMasterKey(seed = seed)
            val pk = providers.secrets.getPrivateKey(key = mk)
            val pub = providers.secrets.getPublicKey(key = pk)
            TODO("UnregisteredLogics:register")
        }
    }
}
