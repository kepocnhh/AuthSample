package test.cmp.auth.provider

import java.security.spec.AlgorithmParameterSpec

internal interface Biometrics<T : AlgorithmParameterSpec> {
    suspend fun encrypt(decrypted: ByteArray): Pair<T, ByteArray>
    suspend fun decrypt(encrypted: ByteArray, spec: T): ByteArray
}
