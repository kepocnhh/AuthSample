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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.withContext
import test.cmp.auth.App

@Composable
internal fun AuthorizedScreen(onLock: () -> Unit) {
    val providers = remember { App.providers }
    val logger = remember { providers.loggers.create("[Authorized]") }
    val logics = App.logics<AuthorizedLogics>()
    val state = logics.states.collectAsState().value
    LaunchedEffect(Unit) {
        withContext(providers.contexts.default) {
            logics.events.collect { event ->
                when (event) {
                    AuthorizedLogics.Event.OnLock -> onLock()
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
