package com.example.appshoptour

import com.example.appshoptour.auth.PasswordHasher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {

    @Test fun `hash produces non-empty result`() = runTest {
        val r = PasswordHasher.hash("secret123")
        assertTrue(r.hash.isNotBlank() && r.salt.isNotBlank())
    }

    @Test fun `same password produces different hashes due to random salt`() = runTest {
        val first = PasswordHasher.hash("secret123")
        val second = PasswordHasher.hash("secret123")
        assertNotEquals(first.hash, second.hash)
        assertNotEquals(first.salt, second.salt)
    }

    @Test fun `verify returns true for correct password`() = runTest {
        val (hash, salt) = PasswordHasher.hash("secret123")
        assertTrue(PasswordHasher.verify("secret123", hash, salt))
    }

    @Test fun `verify returns false for wrong password`() = runTest {
        val (hash, salt) = PasswordHasher.hash("secret123")
        assertFalse(PasswordHasher.verify("wrongpassword", hash, salt))
    }

    @Test fun `verify returns false for tampered hash`() = runTest {
        val (_, salt) = PasswordHasher.hash("secret123")
        assertFalse(PasswordHasher.verify("secret123", "dGFtcGVyZWQ=", salt))
    }
}
