package test.cmp.auth.module.authorized

import java.io.ByteArrayInputStream
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
import sp.kx.bytes.hex
import sp.kx.bytes.readBytes
import sp.kx.bytes.readInt
import sp.kx.bytes.writeBytes
import sp.kx.logics.Logics
import test.cmp.auth.provider.Providers

internal class AuthorizedLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    data class State(val isLoading: Boolean)

    sealed interface Event {
        data object OnLock : Event
        class OnEncrypt(val message: ByteArray) : Event
        class OnDecrypt(val result: Result<ByteArray>) : Event
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
        val message = withContext(providers.contexts.default) {
            val pk = providers.locals.pk ?: error("No private key!")
            val keyPair = providers.secrets.newKeyPair()
            logger.debug("keyPair:pub:sha256: ${providers.hashes.sha256(keyPair.public.encoded).copyOf(16).hex()}")
            val pub = providers.secrets.getPublicKey(pk)
            val sk = providers.secrets.getSharedKey(keyPair.private, pub)
            val id = UUID.randomUUID()
            logger.debug("id: $id")
            val time = System.currentTimeMillis().milliseconds
            logger.debug("time: $time")
            val body = "foo bar baz".toByteArray()
            logger.debug("body:sha256: ${providers.hashes.sha256(body).copyOf(16).hex()}")
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
            logger.debug("spec: ${spec.tLen} ${spec.iv.hex()}")
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
        _events.emit(Event.OnEncrypt(message = message))
    }

    fun decrypt(message: ByteArray) = launch {
        logger.debug("decrypt")
        _states.value = State(isLoading = true)
        val result = withContext(providers.contexts.default) {
            runCatching {
                ByteArrayInputStream(message).use { stream ->
                    val pub = providers.secrets.toPublicKey(stream.readBytes(stream.readInt()))
                    logger.debug("keyPair:pub:sha256: ${providers.hashes.sha256(pub.encoded).copyOf(16).hex()}")
                    val spec = GCMParameterSpec(stream.read(), stream.readBytes(12))
                    logger.debug("spec: ${spec.tLen} ${spec.iv.hex()}")
                    val encrypted = stream.readBytes(stream.readInt())
                    val signature = stream.readBytes(stream.readInt())
                }
                TODO("AuthorizedLogics:decrypt")
            }
        }
        _states.value = State(isLoading = false)
        _events.emit(Event.OnDecrypt(result))
    }
}
