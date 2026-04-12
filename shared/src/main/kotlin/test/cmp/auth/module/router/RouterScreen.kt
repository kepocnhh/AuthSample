package test.cmp.auth.module.router

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import test.cmp.auth.App
import test.cmp.auth.module.authorized.AuthorizedScreen
import test.cmp.auth.module.authorized.UnauthorizedScreen
import test.cmp.auth.module.registered.UnregisteredScreen

@Composable
internal fun RouterScreen() {
    val logics = App.logics<RouterLogics>()
    val state = logics.states.collectAsState().value
    LaunchedEffect(Unit) {
        logics.requestState()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        when (state) {
            is RouterLogics.State.Authorized -> {
                AuthorizedScreen(
                    ek = state.ek,
                    onLock = {
                        logics.requestState()
                    },
                )
            }
            is RouterLogics.State.Unauthorized -> {
                UnauthorizedScreen(
                    ek = state.ek,
                    onUnlock = {
                        logics.requestState()
                    },
                    onExit = {
                        logics.requestState()
                    },
                )
            }
            RouterLogics.State.Unregistered -> {
                UnregisteredScreen(
                    onRegister = {
                        logics.requestState()
                    },
                )
            }
            null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                ) {
                    BasicText(text = "loading...")
                }
            }
        }
    }
}
