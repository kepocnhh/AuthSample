package test.cmp.auth

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import test.cmp.auth.module.router.RouterScreen
import test.cmp.auth.provider.Contexts
import test.cmp.auth.provider.Providers

internal object App {
    val providers: Providers

    init {
        val contexts = Contexts(
            main = Dispatchers.Main,
            default = Dispatchers.Default,
        )
        providers = Providers(
            contexts = contexts,
        )
    }
}

fun main() {
    application {
        Window(onCloseRequest = ::exitApplication) {
            RouterScreen()
        }
    }
}
