package test.cmp.auth.module.registered

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import sp.kx.bytes.readUUID
import sp.kx.logics.Logics
import test.cmp.auth.entity.CipherSpec
import test.cmp.auth.entity.EncryptedKey
import test.cmp.auth.provider.Providers

internal class UnregisteredLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    data class State(val isLoading: Boolean)

    sealed interface Event {
        data class OnRegister(val result: Result<Unit>) : Event
    }

    private val logger = providers.loggers.create("[Unregistered]")
    private val _events = MutableSharedFlow<Event>()
    val events: Flow<Event> = _events.asSharedFlow()
    private val _states = MutableStateFlow<State>(State(isLoading = false))
    val states = _states.asStateFlow()

    fun register(passphrase: String) = launch {
        logger.debug("register")
        _states.value = State(isLoading = true)
        val result = withContext(providers.contexts.default) {
            val seed = providers.secrets.getSeed(passphrase = passphrase)
            val mk = providers.secrets.getMasterKey(seed = seed)
            val pk = providers.secrets.getPrivateKey(key = mk)
            runCatching {
                val (spec, encrypted) = providers.biometrics.encrypt(decrypted = pk.encoded)
                val pub = providers.secrets.getPublicKey(key = pk)
                val id = providers.hashes.sha256(pub.encoded).readUUID()
                val ek = EncryptedKey(
                    id = id,
                    cs = CipherSpec(
                        iterations = 0, // todo
                        salt = byteArrayOf(), // todo
                        spec = spec,
                    ),
                    encoded = encrypted,
                )
                providers.locals.ek = ek
                providers.locals.pk = pk
            }
        }
        _states.value = State(isLoading = false)
        _events.emit(Event.OnRegister(result = result))
    }
}
