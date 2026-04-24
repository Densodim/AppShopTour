package com.example.appshoptour.data.repository

import com.example.appshoptour.data.remote.AuthApiClient
import com.example.appshoptour.data.remote.TokenStorage
import com.example.appshoptour.domain.repository.AuthRepository

class AuthRepositoryImpl (
    private val authApiClient: AuthApiClient,
    private val tokenStorage: TokenStorage
): AuthRepository{
    override suspend fun login(email: String, password: String) {
        val response = authApiClient.login(email, password)
        tokenStorage.saveTokens(response.accessToken, response.refreshToken)
    }

    override suspend fun register(name: String, email: String, password: String) {
        val response = authApiClient.register(name, email, password)
        tokenStorage.saveTokens(response.accessToken, response.refreshToken)
    }
}