package test.cmp.auth.provider

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object FinalLoggers : Loggers {
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.US)

    override fun create(tag: String): Logger {
        return FinalLogger(tag = tag, dateFormat = dateFormat)
    }
}

private class FinalLogger(
    private val tag: String,
    private val dateFormat: DateFormat,
) : Logger {
    override fun debug(message: String) {
        val text = """
            ${dateFormat.format(Date())} $tag Debug
            $message
            
        """.trimIndent()
        System.out.println(text)
    }

    override fun warning(message: String) {
        val text = """
            ${dateFormat.format(Date())} $tag Warning
            $message
            
        """.trimIndent()
        System.err.println(text)
    }
}
