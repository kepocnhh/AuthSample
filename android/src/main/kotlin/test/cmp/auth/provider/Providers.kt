package test.cmp.auth.provider

import javax.crypto.spec.GCMParameterSpec

internal class Providers(
    val contexts: Contexts,
    val dirs: Dirs,
    val locals: Locals,
    val loggers: Loggers,
    val secrets: Secrets,
    val hashes: Hashes,
    val transformers: Transformers,
    val biometrics: Biometrics<GCMParameterSpec>,
)
