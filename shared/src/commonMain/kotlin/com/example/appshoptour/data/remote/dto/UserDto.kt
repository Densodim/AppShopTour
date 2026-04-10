package com.example.appshoptour.data.remote.dto

import com.example.appshoptour.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO (Data Transfer Object) — модель для JSON сериализации.
 * Отделена от доменной модели: сервер может менять поля,
 * а доменная модель остаётся стабильной.
 */
@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("preferred_currency") val preferredCurrency: String = "EUR",
    @SerialName("preferred_language") val preferredLanguage: String = "ru",
    @SerialName("theme_mode") val themeMode: String = "dark"
)

fun UserDto.toDomain() = User(
    id = id,
    name = name,
    email = email,
    preferredCurrency = preferredCurrency,
    preferredLanguage = preferredLanguage,
    themeMode = themeMode
)
