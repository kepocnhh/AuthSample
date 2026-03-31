package test.cmp.auth.module.router

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import sp.kx.logics.Logics
import test.cmp.auth.provider.Providers

internal class RouterLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    sealed interface State {
        data object Authorized : State
        data object Unauthorized : State
        data object Unregistered : State
    }

    private val logger = providers.loggers.create("[Router]")
    private val _states = MutableStateFlow<State?>(null)
    val states = _states.asStateFlow()

    fun requestState() = launch {
        logger.debug("request state")
        _states.value = null
        val ek = withContext(providers.contexts.default) {
            providers.locals.ek
        }
        if (ek == null) {
            _states.value = State.Unregistered
        } else {
            val pk = withContext(providers.contexts.default) {
                providers.locals.pk
            }
            if (pk == null) {
                _states.value = State.Unauthorized
            } else {
                _states.value = State.Authorized
            }
        }
    }
}
