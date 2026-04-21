package com.example.appshoptour.auth

import com.example.appshoptour.api.dto.AuthResponse

class FakeAuthService : AuthService {
    var shouldFailLogin = false
    val registeredEmails = mutableSetOf<String>()

    override suspend fun register(name: String, email: String, password: String): AuthResponse {
        if (email in registeredEmails) throw AuthError.EmailAlreadyExists
        registeredEmails += email
        return AuthResponse(accessToken = "fake-access", refreshToken = "fake-refresh")
    }

    override suspend fun login(email: String, password: String): AuthResponse {
        if (shouldFailLogin || email !in registeredEmails) throw AuthError.InvalidCredentials
        return AuthResponse(accessToken = "fake-access", refreshToken = "fake-refresh")
    }

    override suspend fun refresh(refreshToken: String): AuthResponse {
        if (refreshToken == "invalid") throw AuthError.TokenInvalid
        return AuthResponse(accessToken = "new-fake-access", refreshToken = "new-fake-refresh")
    }

    override suspend fun logout(userId: String) {}
}
