package com.example.appshoptour

import com.example.appshoptour.auth.AuthError
import com.example.appshoptour.auth.FakeAuthService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AuthServiceTest {

    private val fakeAuth = FakeAuthService()

    @Test fun `register returns tokens`() = runTest {
        val result = fakeAuth.register("Test", "test@test.com", "pass123")
        assertTrue(result.accessToken.isNotBlank())
        assertTrue(result.refreshToken.isNotBlank())
    }

    @Test fun `register throws EmailAlreadyExists on duplicate`() = runTest {
        fakeAuth.register("Test", "dup@test.com", "pass123")
        assertFailsWith<AuthError.EmailAlreadyExists> {
            fakeAuth.register("Test2", "dup@test.com", "pass123")
        }
    }

    @Test fun `login throws InvalidCredentials when shouldFailLogin`() = runTest {
        fakeAuth.registeredEmails += "test@test.com"
        fakeAuth.shouldFailLogin = true
        assertFailsWith<AuthError.InvalidCredentials> {
            fakeAuth.login("test@test.com", "wrong")
        }
    }

    @Test fun `login throws InvalidCredentials for unknown email`() = runTest {
        assertFailsWith<AuthError.InvalidCredentials> {
            fakeAuth.login("nobody@test.com", "pass")
        }
    }

    @Test fun `refresh throws TokenInvalid for invalid token`() = runTest {
        assertFailsWith<AuthError.TokenInvalid> {
            fakeAuth.refresh("invalid")
        }
    }
}
