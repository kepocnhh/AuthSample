package test.cmp.auth.module.authorized

import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.crypto.spec.GCMParameterSpec
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import sp.kx.bytes.writeBytes
import sp.kx.logics.Logics
import test.cmp.auth.provider.Providers

internal class AuthorizedLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    data class State(val isLoading: Boolean)

    sealed interface Event {
        data object OnLock : Event
        class OnEncrypt(val payload: ByteArray) : Event
    }

    private val logger = providers.loggers.create("[Authorized]")
    private val _events = MutableSharedFlow<Event>()
    val events: Flow<Event> = _events.asSharedFlow()
    private val _states = MutableStateFlow<State>(State(isLoading = false))
    val states = _states.asStateFlow()

    fun lock() = launch {
        logger.debug("lock")
        _states.value = State(isLoading = true)
        withContext(providers.contexts.default) {
            providers.locals.pk = null
        }
        _events.emit(Event.OnLock)
    }

    fun encrypt() = launch {
        logger.debug("encrypt")
        _states.value = State(isLoading = true)
        val payload = withContext(providers.contexts.default) {
            val pk = providers.locals.pk ?: error("No private key!")
            val keyPair = providers.secrets.newKeyPair()
            val pub = providers.secrets.getPublicKey(pk)
            val sk = providers.secrets.getSharedKey(keyPair.private, pub)
            val id = UUID.randomUUID()
            val time = System.currentTimeMillis().milliseconds
            val body = "foo bar baz".toByteArray()
            val signee = ByteArrayOutputStream().use { stream ->
                stream.writeBytes(id)
                stream.writeBytes(time.inWholeMilliseconds)
                stream.writeBytes(body)
                stream.toByteArray()
            }
            val signature = providers.secrets.sign(pk, signee)
            val decrypted = ByteArrayOutputStream().use { stream ->
                stream.writeBytes(id)
                stream.writeBytes(time.inWholeMilliseconds)
                stream.writeBytes(body.size)
                stream.writeBytes(body)
                stream.toByteArray()
            }
            val iv = ByteArray(12)
            providers.secrets.nextBytes(iv)
            val spec = GCMParameterSpec(128, iv)
            val encrypted = providers.secrets.encrypt(sk, decrypted, spec)
            ByteArrayOutputStream().use { stream ->
                stream.writeBytes(keyPair.public.encoded.size)
                stream.writeBytes(keyPair.public.encoded)
                stream.write(spec.tLen)
                stream.writeBytes(spec.iv)
                stream.writeBytes(encrypted.size)
                stream.writeBytes(encrypted)
                stream.writeBytes(signature.size)
                stream.writeBytes(signature)
                stream.toByteArray()
            }
        }
        _states.value = State(isLoading = false)
        _events.emit(Event.OnEncrypt(payload))
    }

    fun decrypt(payload: ByteArray) = launch {
        logger.debug("decrypt")
        // todo
    }
}
