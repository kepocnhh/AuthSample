package test.cmp.auth.module.registered

import java.util.UUID
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import sp.kx.bytes.readLong
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
        data object OnRegister : Event
        data object OnEnter : Event
    }

    private val logger = providers.loggers.create("[Unregistered]")
    private val _events = MutableSharedFlow<Event>()
    val events: Flow<Event> = _events.asSharedFlow()
    private val _states = MutableStateFlow<State>(State(isLoading = false))
    val states = _states.asStateFlow()
    private val _keys = MutableStateFlow<List<EncryptedKey>>(emptyList())
    val keys = _keys.asStateFlow()

    private fun getKeys(): List<EncryptedKey> {
        val list = mutableListOf<EncryptedKey>()
        for (file in providers.dirs.keys.listFiles() ?: return emptyList()) {
            val ek = try {
                providers.transformers.ek.decode(file.readBytes())
            } catch (error: Throwable) {
                logger.warning("read file ${file.name} error: $error")
                continue
            }
            list.add(ek)
        }
        return list
    }

    fun requestKeys() = launch {
        logger.debug("request keys")
        _keys.value = withContext(providers.contexts.default) {
            getKeys()
        }
    }

    fun register(passphrase: String, password: String) = launch {
        logger.debug("register")
        _states.value = State(isLoading = true)
        withContext(providers.contexts.default) {
            val seed = providers.secrets.getSeed(passphrase = passphrase)
            val mk = providers.secrets.getMasterKey(seed = seed)
            val pk = providers.secrets.getPrivateKey(key = mk)
            val salt = ByteArray(32)
            providers.secrets.nextBytes(salt)
            val iterations = 600_000
            val sk = providers.secrets.getSecretKey(
                password = password,
                salt = salt,
                iterations = iterations,
                keySize = 256,
            )
            val iv = ByteArray(12)
            providers.secrets.nextBytes(iv)
            val pub = providers.secrets.getPublicKey(key = pk)
            val id = providers.hashes.sha256(pub.encoded).readUUID()
            val spec = GCMParameterSpec(128, iv)
            val ek = EncryptedKey(
                id = id,
                cs = CipherSpec(
                    iterations = iterations,
                    salt = salt,
                    spec = spec,
                ),
                encoded = providers.secrets.encrypt(key = sk, decrypted = pk.encoded, spec = spec),
            )
            val file = providers.dirs.keys.resolve("$id.bin")
            if (file.exists()) TODO("UnregisteredLogics:register")
            file.writeBytes(providers.transformers.ek.encode(decoded = ek))
            providers.locals.ek = ek
            providers.locals.pk = pk
        }
        _events.emit(Event.OnRegister)
    }

    fun enter(ek: EncryptedKey) = launch {
        logger.debug("enter")
        withContext(providers.contexts.default) {
            providers.locals.ek = ek
        }
        _events.emit(Event.OnEnter)
    }
}
