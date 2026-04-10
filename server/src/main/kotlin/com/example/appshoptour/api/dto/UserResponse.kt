package com.example.appshoptour.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для ответа API — то что клиент получает в JSON.
 * Отдельный класс от UsersTable чтобы не раскрывать password_hash клиенту.
 */
@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("preferred_currency") val preferredCurrency: String,
    @SerialName("preferred_language") val preferredLanguage: String,
    @SerialName("theme_mode") val themeMode: String
)
