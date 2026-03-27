package test.cmp.auth.module.registered

import kotlinx.coroutines.withContext
import sp.kx.logics.Logics
import test.cmp.auth.provider.Providers

internal class UnregisteredLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    fun register(passphrase: String) = launch {
        withContext(providers.contexts.default) {
            TODO()
        }
    }
}
