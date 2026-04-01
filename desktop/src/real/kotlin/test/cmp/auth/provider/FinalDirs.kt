package test.cmp.auth.provider

import java.io.File

internal class FinalDirs : Dirs {
    private val userHome = System.getProperty("user.home") ?: error("No property!")

    override val keys: File
        get() {
            val file = File(userHome)
                .resolve(".local/share/keys")
            if (file.exists()) {
                check(file.isDirectory)
            } else {
                check(file.mkdirs())
            }
            return file
        }
}
