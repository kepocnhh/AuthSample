package test.cmp.auth.provider

import java.io.File

internal class FinalDirs : Dirs {
    private val userHome = System.getProperty("user.home") ?: error("No property!")

    override val files: File
        get() {
            val file = File(userHome)
                .resolve(".local")
                .resolve("test.cmp.auth") // todo
            if (file.exists()) {
                check(file.isDirectory)
            } else {
                check(file.mkdirs())
            }
            return file
        }

    override val cache: File
        get() {
            val file = File(userHome)
                .resolve(".cache")
                .resolve("test.cmp.auth") // todo
            if (file.exists()) {
                check(file.isDirectory)
            } else {
                check(file.mkdirs())
            }
            return file
        }
}
