package com.example.appshoptour.auth

import com.example.appshoptour.api.dto.AuthResponse
import com.example.appshoptour.api.dto.UserResponse

interface AuthService {
    suspend fun register(name: String, email: String, password: String): AuthResponse
    suspend fun login(email: String, password: String): AuthResponse
    suspend fun refresh(refreshToken: String): AuthResponse
    suspend fun logout(userId: String)
}
