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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            val text = state?.let { it::class.java.name } ?: "loading..."
            BasicText(text = text)
        }
    }
}
