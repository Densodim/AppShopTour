package com.example.appshoptour.auth

import dev.whyoleg.cryptography.BinarySize.Companion.bytes
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.PBKDF2
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.random.CryptographyRandom
import java.security.MessageDigest
import java.util.Base64

object PasswordHasher {
    private val provider = CryptographyProvider.Default
    private const val ITERATIONS = 600_000
    private const val KEY_BYTES = 32
    private const val SALT_BYTES = 16

    data class HashedPassword(val hash: String, val salt: String)

    suspend fun hash(password: String): HashedPassword {
        val salt = CryptographyRandom.nextBytes(SALT_BYTES)
        val derived = derive(password, salt)
        return HashedPassword(
            hash = Base64.getEncoder().encodeToString(derived),
            salt = Base64.getEncoder().encodeToString(salt)
        )
    }

    suspend fun verify(password: String, storedHash: String, storedSalt: String): Boolean {
        val salt = Base64.getDecoder().decode(storedSalt)
        val derived = derive(password, salt)
        return MessageDigest.isEqual(derived, Base64.getDecoder().decode(storedHash))
    }

    private suspend fun derive(password: String, salt: ByteArray): ByteArray =
        provider.get(PBKDF2)
            .secretDerivation(SHA256, ITERATIONS, KEY_BYTES.bytes, salt)
            .deriveSecretBlocking(password.encodeToByteArray())
            .toByteArray()
}
