package test.cmp.auth.module.registered

import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import sp.kx.bytes.writeBytes
import sp.kx.logics.Logics
import test.cmp.auth.entity.EncryptedKey
import test.cmp.auth.provider.Providers

internal class UnregisteredLogics(
    private val providers: Providers,
) : Logics(providers.contexts.main) {
    sealed interface Event {
        data object OnRegister : Event
    }

    private val logger = providers.loggers.create("[Unregistered]")
    private val _events = MutableSharedFlow<Event>()
    val events: Flow<Event> = _events.asSharedFlow()

    fun register(passphrase: String, password: String) = launch {
        withContext(providers.contexts.default) {
            val seed = providers.secrets.getSeed(passphrase = passphrase)
            val mk = providers.secrets.getMasterKey(seed = seed)
            val pk = providers.secrets.getPrivateKey(key = mk)
            val salt = ByteArray(32)
            providers.secrets.nextBytes(salt)
            val iterations = 700_000
            val sk = providers.secrets.getSecretKey(
                password = password,
                salt = salt,
                iterations = iterations,
                keyLength = 256,
            )
            logger.debug("iterations: $iterations")
            logger.debug("keyLength: ${sk.encoded.size}")
            val nonce = ByteArray(12)
            providers.secrets.nextBytes(nonce)
            val encrypted = providers.secrets.encrypt(key = sk, decrypted = pk.encoded, nonce = nonce)
            val bytes = ByteArrayOutputStream().use { stream ->
                stream.writeBytes(iterations)
                stream.writeBytes(salt.size)
                stream.writeBytes(salt)
                stream.writeBytes(nonce.size)
                stream.writeBytes(nonce)
                stream.writeBytes(encrypted.size)
                stream.writeBytes(encrypted)
                stream.toByteArray()
            }
            TODO("UnregisteredLogics:register")
            providers.locals.key = EncryptedKey(encoded = bytes)
        }
    }
}
