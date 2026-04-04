package test.cmp.auth.provider

import test.cmp.auth.util.Biometrics

internal class Providers(
    val contexts: Contexts,
    val dirs: Dirs,
    val locals: Locals,
    val loggers: Loggers,
    val secrets: Secrets,
    val hashes: Hashes,
    val transformers: Transformers,
    val biometrics: Biometrics,
)
