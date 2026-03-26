package test.cmp.auth.provider

import android.util.Log
import test.cmp.auth.provider.Logger
import test.cmp.auth.provider.Loggers

internal object FinalLoggers : Loggers {
    override fun create(tag: String): Logger {
        return FinalLogger(tag = tag)
    }
}

private class FinalLogger(
    private val tag: String,
) : Logger {
    override fun debug(message: String) {
        Log.d(tag, message)
    }

    override fun warning(message: String) {
        Log.w(tag, message)
    }
}
