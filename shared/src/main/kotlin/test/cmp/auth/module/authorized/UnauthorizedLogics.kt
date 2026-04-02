package test.cmp.auth.module.authorized

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import sp.kx.bytes.toByteArray
import sp.kx.logics.Logics
import test.cmp.auth.provider.Providers

internal class UnauthorizedLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    data class State(val isLoading: Boolean)

    sealed interface Event {
        class OnUnlock(val result: Result<Unit>) : Event
        data object OnExit : Event
    }

    private val logger = providers.loggers.create("[Unauthorized]")
    private val _events = MutableSharedFlow<Event>()
    val events: Flow<Event> = _events.asSharedFlow()
    private val _states = MutableStateFlow<State>(State(isLoading = false))
    val states = _states.asStateFlow()

    fun unlock(password: String) = launch {
        logger.debug("unlock")
        _states.value = State(isLoading = true)
        val result = withContext(providers.contexts.default) {
            runCatching {
                val ek = providers.locals.ek ?: TODO("no ek!")
                val sk = providers.secrets.getSecretKey(
                    password = password,
                    salt = ek.cs.salt,
                    iterations = ek.cs.iterations,
                    keyLength = 256,
                )
                val encoded = providers.secrets.decrypt(key = sk, encrypted = ek.encoded, nonce = ek.cs.nonce)
                val pk = providers.secrets.toPrivateKey(encoded = encoded)
                val pub = providers.secrets.getPublicKey(key = pk)
                val expected = providers.hashes.sha256(pub.encoded).copyOf(16)
                val actual = ek.id.toByteArray()
                if (!expected.contentEquals(actual)) TODO("UnauthorizedLogics:unlock")
                providers.locals.pk = pk
            }
        }
        _events.emit(Event.OnUnlock(result = result))
        _states.value = State(isLoading = false)
    }

    fun exit() = launch {
        logger.debug("exit")
        _states.value = State(isLoading = true)
        withContext(providers.contexts.default) {
            providers.locals.ek = null
        }
        _events.emit(Event.OnExit)
    }
}
