package test.cmp.auth.provider

import android.content.Context
import java.io.File

internal class FinalDirs(context: Context) : Dirs {
    private val files = context.filesDir ?: error("No files!")
    override val keys: File
        get() {
            val file = files.resolve("keys")
            if (file.exists()) {
                check(file.isDirectory)
            } else {
                check(file.mkdirs())
            }
            return file
        }
}
