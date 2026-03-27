package test.cmp.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import kotlinx.coroutines.Dispatchers
import sp.kx.logics.Logics
import sp.kx.logics.LogicsFactory
import sp.kx.logics.LogicsProvider
import test.cmp.auth.module.router.RouterScreen
import test.cmp.auth.provider.Contexts
import test.cmp.auth.provider.Dirs
import test.cmp.auth.provider.FinalDirs
import test.cmp.auth.provider.FinalLocals
import test.cmp.auth.provider.FinalLoggers
import test.cmp.auth.provider.FinalSecrets
import test.cmp.auth.provider.Locals
import test.cmp.auth.provider.Loggers
import test.cmp.auth.provider.Providers
import test.cmp.auth.provider.Secrets

internal object App {
    val providers: Providers

    private val _logicsProvider = LogicsProvider(
        factory = object : LogicsFactory {
            override fun <T : Logics> create(type: Class<T>): T {
                return type
                    .getConstructor(Providers::class.java)
                    .newInstance(providers)
            }
        },
    )

    init {
        val contexts = Contexts(
            main = Dispatchers.Main,
            default = Dispatchers.Default,
        )
        val dirs: Dirs = FinalDirs()
        val locals: Locals = FinalLocals(dirs = dirs)
        val loggers: Loggers = FinalLoggers
        val secrets: Secrets = FinalSecrets()
        providers = Providers(
            contexts = contexts,
            dirs = dirs,
            locals = locals,
            loggers = loggers,
            secrets = secrets,
        )
    }

    @Composable
    inline fun <reified T : Logics> logics(label: String = T::class.java.name): T {
        val contains = remember { _logicsProvider.contains(label = label, T::class.java) }
        val logics = _logicsProvider.get(label = label, T::class.java)
        DisposableEffect(Unit) {
            onDispose {
                if (!contains) _logicsProvider.remove(label = label, T::class.java)
            }
        }
        return logics
    }
}

fun main() {
    application {
        Window(onCloseRequest = ::exitApplication) {
            RouterScreen()
        }
    }
}
