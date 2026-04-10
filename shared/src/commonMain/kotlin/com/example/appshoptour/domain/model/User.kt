package com.example.appshoptour.domain.model

/**
 * Доменная модель пользователя.
 * Чистый Kotlin data class — никаких платформенных зависимостей.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val preferredCurrency: String = "EUR",
    val preferredLanguage: String = "ru",
    val themeMode: String = "dark"
)
