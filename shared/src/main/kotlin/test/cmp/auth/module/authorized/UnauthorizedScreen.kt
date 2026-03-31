package test.cmp.auth.module.authorized

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
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

@Composable
internal fun UnauthorizedScreen(
    onAuthorize: () -> Unit,
    onExit: () -> Unit,
) {
    val providers = remember { App.providers }
    val logics = App.logics<UnauthorizedLogics>()
    val state = logics.states.collectAsState().value
    val passwords = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        withContext(providers.contexts.default) {
            logics.events.collect { event ->
                when (event) {
                    UnauthorizedLogics.Event.OnAuthorize -> onAuthorize()
                    UnauthorizedLogics.Event.OnExit -> onExit()
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
                text = "enter your password",
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
            val password = passwords.value
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = password.isNotEmpty() && !state.isLoading) {
                        logics.authorize(password = password)
                    }
                    .padding(16.dp)
                    .wrapContentSize(),
                text = "authorize",
            )
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(!state.isLoading) {
                        logics.exit()
                    }
                    .padding(16.dp)
                    .wrapContentSize(),
                text = "exit",
            )
        }
    }
}
