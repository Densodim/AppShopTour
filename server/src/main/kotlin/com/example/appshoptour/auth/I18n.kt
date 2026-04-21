package com.example.appshoptour.auth

object I18n {
    enum class Lang { EN, RU, ES }

    enum class MessageKey {
        INVALID_CREDENTIALS, EMAIL_ALREADY_EXISTS,
        TOKEN_INVALID, TOKEN_EXPIRED,
        VALIDATION_FAILED, LOGOUT_SUCCESS, INTERNAL_ERROR,
        RATE_LIMIT_EXCEEDED
    }

    fun resolve(acceptLanguage: String?, jwtLang: String? = null): Lang {
        val raw = jwtLang ?: acceptLanguage?.split(",")?.firstOrNull()?.take(2)
        return when (raw?.lowercase()) {
            "ru" -> Lang.RU
            "es" -> Lang.ES
            else -> Lang.EN
        }
    }

    fun message(key: MessageKey, lang: Lang): String =
        messages[key]?.get(lang) ?: messages[key]?.get(Lang.EN) ?: key.name

    private val messages: Map<MessageKey, Map<Lang, String>> = mapOf(
        MessageKey.INVALID_CREDENTIALS to mapOf(
            Lang.EN to "Invalid email or password",
            Lang.RU to "Неверный email или пароль",
            Lang.ES to "Correo o contraseña incorrectos"
        ),
        MessageKey.EMAIL_ALREADY_EXISTS to mapOf(
            Lang.EN to "Email is already registered",
            Lang.RU to "Email уже зарегистрирован",
            Lang.ES to "El correo ya está registrado"
        ),
        MessageKey.TOKEN_INVALID to mapOf(
            Lang.EN to "Token is invalid",
            Lang.RU to "Токен недействителен",
            Lang.ES to "El token no es válido"
        ),
        MessageKey.TOKEN_EXPIRED to mapOf(
            Lang.EN to "Token has expired",
            Lang.RU to "Срок действия токена истёк",
            Lang.ES to "El token ha expirado"
        ),
        MessageKey.VALIDATION_FAILED to mapOf(
            Lang.EN to "Validation error",
            Lang.RU to "Ошибка валидации",
            Lang.ES to "Error de validación"
        ),
        MessageKey.LOGOUT_SUCCESS to mapOf(
            Lang.EN to "Logged out successfully",
            Lang.RU to "Выход выполнен успешно",
            Lang.ES to "Sesión cerrada correctamente"
        ),
        MessageKey.INTERNAL_ERROR to mapOf(
            Lang.EN to "Internal server error",
            Lang.RU to "Внутренняя ошибка сервера",
            Lang.ES to "Error interno del servidor"
        ),
        MessageKey.RATE_LIMIT_EXCEEDED to mapOf(
            Lang.EN to "Too many requests, please try again later",
            Lang.RU to "Слишком много запросов, попробуйте позже",
            Lang.ES to "Demasiadas solicitudes, intente más tarde"
        ),
    )
}
