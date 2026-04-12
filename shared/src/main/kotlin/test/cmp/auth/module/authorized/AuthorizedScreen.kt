package test.cmp.auth.module.authorized

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.withContext
import test.cmp.auth.App
import test.cmp.auth.entity.EncryptedKey

@Composable
internal fun AuthorizedScreen(
    ek: EncryptedKey,
    onLock: () -> Unit,
) {
    val providers = remember { App.providers }
    val logger = remember { providers.loggers.create("[Authorized]") }
    val logics = App.logics<AuthorizedLogics>()
    val state = logics.states.collectAsState().value
    LaunchedEffect(Unit) {
        withContext(providers.contexts.default) {
            logics.events.collect { event ->
                when (event) {
                    AuthorizedLogics.Event.OnLock -> onLock()
                    is AuthorizedLogics.Event.OnEncrypt -> {
                        logger.debug("on encrypt: ${event.payload.size}")
                        logics.decrypt(payload = event.payload)
                    }
                    is AuthorizedLogics.Event.OnDecrypt -> {
                        event.result.fold(
                            onSuccess = { body ->
                                logger.debug("on decrypt: ${String(body)}")
                            },
                            onFailure = { error ->
                                logger.warning("on decrypt error: $error")
                            },
                        )
                    }
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = ek.id.toString(),
                style = TextStyle(fontFamily = FontFamily.Monospace),
            )
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(!state.isLoading) {
                        logics.encrypt()
                    }
                    .padding(16.dp)
                    .wrapContentSize(),
                text = "encrypt",
            )
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(!state.isLoading) {
                        logics.lock()
                    }
                    .padding(16.dp)
                    .wrapContentSize(),
                text = "lock",
            )
        }
    }
}
