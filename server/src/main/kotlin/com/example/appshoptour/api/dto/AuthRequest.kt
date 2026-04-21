package com.example.appshoptour.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String) {
    fun validate(): List<String> = buildList {
        if (name.isBlank()) add("Name is required")
        if (!email.contains("@") || !email.contains(".")) add("Invalid email format")
        if (password.length < 8) add("Password must be at least 8 characters")
    }
}

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RefreshRequest(@SerialName("refresh_token") val refreshToken: String)
