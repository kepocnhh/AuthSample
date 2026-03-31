package test.cmp.auth.module.authorized

import java.io.ByteArrayInputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import sp.kx.bytes.readBytes
import sp.kx.bytes.readInt
import sp.kx.logics.Logics
import test.cmp.auth.provider.Providers

internal class UnauthorizedLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    data class State(val isLoading: Boolean)

    sealed interface Event {
        data object OnAuthorize : Event
    }

    private val logger = providers.loggers.create("[Unauthorized]")
    private val _events = MutableSharedFlow<Event>()
    val events: Flow<Event> = _events.asSharedFlow()
    private val _states = MutableStateFlow<State>(State(isLoading = false))
    val states = _states.asStateFlow()

    fun authorize(password: String) = launch {
        _states.value = State(isLoading = true)
        withContext(providers.contexts.default) {
            val ek = providers.locals.ek ?: TODO("no ek!")
            ByteArrayInputStream(ek.encoded).use { stream ->
                val iterations = stream.readInt()
                val salt = stream.readBytes(stream.readInt())
                val sk = providers.secrets.getSecretKey(
                    password = password,
                    salt = salt,
                    iterations = iterations,
                    keyLength = 256,
                )
                val nonce = stream.readBytes(stream.readInt())
                val encrypted = stream.readBytes(stream.readInt())
                val decrypted = providers.secrets.decrypt(key = sk, encrypted = encrypted, nonce = nonce)
                providers.locals.pk = providers.secrets.getPrivateKey(encoded = decrypted)
            }
        }
        _events.emit(Event.OnAuthorize)
    }
}
