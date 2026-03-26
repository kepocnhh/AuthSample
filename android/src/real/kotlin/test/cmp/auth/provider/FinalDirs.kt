package test.cmp.auth.provider

import android.content.Context
import test.cmp.auth.provider.Dirs

internal class FinalDirs(context: Context) : Dirs {
    override val files = context.filesDir ?: error("No files!")
    override val cache = context.cacheDir ?: error("No cache!")
}
