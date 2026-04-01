package test.cmp.auth.module.registered

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.withContext
import sp.kx.bytes.hex
import test.cmp.auth.App

@Composable
internal fun UnregisteredScreen(
    onEnter: () -> Unit,
) {
    val providers = remember { App.providers }
    val logger = remember { providers.loggers.create("[Unregistered]") }
    val logics = App.logics<UnregisteredLogics>()
    val state = logics.states.collectAsState().value
    val keys = logics.keys.collectAsState().value
    LaunchedEffect(Unit) {
        logics.requestKeys()
    }
    val passphrases = remember { mutableStateOf("") }
    val passwords = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        withContext(providers.contexts.default) {
            logics.events.collect { event ->
                when (event) {
                    UnregisteredLogics.Event.OnEnter -> onEnter()
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Spacer(Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = "passphrase",
            )
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(color = Color.LightGray, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .wrapContentHeight(),
                value = passphrases.value,
                readOnly = state.isLoading,
                onValueChange = { value ->
                    if (value.length < 32) {
                        passphrases.value = value
                    }
                },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace),
            )
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = "password",
            )
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(color = Color.LightGray, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .wrapContentHeight(),
                value = passwords.value,
                readOnly = state.isLoading,
                onValueChange = { value ->
                    if (value.length < 32) {
                        passwords.value = value
                    }
                },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace),
            )
            val passphrase = passphrases.value
            val password = passwords.value
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = passphrase.isNotEmpty() && password.isNotEmpty() && !state.isLoading) {
                        logics.enter(passphrase = passphrase, password = password)
                    }
                    .padding(16.dp)
                    .wrapContentSize(),
                text = "enter",
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (keys.isNotEmpty()) {
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    text = "or enter as",
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (ek in keys) {
                        item {
                            val text = """
                                ${providers.hashes.sha256(ek.pub.encoded).copyOf(8).hex()}
                                ${providers.hashes.sha256(ek.cs.salt).copyOf(8).hex()}
                            """.trimIndent()
                            BasicText(
                                modifier = Modifier
                                    .background(color = Color.LightGray, shape = RoundedCornerShape(16.dp))
                                    .clip(shape = RoundedCornerShape(16.dp))
                                    .clickable {
                                        logics.enter(ek = ek)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .wrapContentHeight(),
                                text = text,
                            )
                        }
                    }
                }
            }
        }
    }
}
