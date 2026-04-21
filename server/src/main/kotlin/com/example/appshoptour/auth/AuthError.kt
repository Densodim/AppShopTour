package com.example.appshoptour.auth

import io.ktor.http.HttpStatusCode

sealed class AuthError : Exception() {
    data object InvalidCredentials : AuthError()
    data object EmailAlreadyExists : AuthError()
    data object TokenInvalid : AuthError()
    data object TokenExpired : AuthError()
    data class ValidationFailed(val fields: List<String>) : AuthError()
}

fun AuthError.toMessageKey(): I18n.MessageKey = when (this) {
    is AuthError.InvalidCredentials -> I18n.MessageKey.INVALID_CREDENTIALS
    is AuthError.EmailAlreadyExists -> I18n.MessageKey.EMAIL_ALREADY_EXISTS
    is AuthError.TokenInvalid       -> I18n.MessageKey.TOKEN_INVALID
    is AuthError.TokenExpired       -> I18n.MessageKey.TOKEN_EXPIRED
    is AuthError.ValidationFailed   -> I18n.MessageKey.VALIDATION_FAILED
}

fun AuthError.toHttpStatus(): HttpStatusCode = when (this) {
    is AuthError.InvalidCredentials -> HttpStatusCode.Unauthorized
    is AuthError.EmailAlreadyExists -> HttpStatusCode.Conflict
    is AuthError.ValidationFailed   -> HttpStatusCode.BadRequest
    is AuthError.TokenInvalid,
    is AuthError.TokenExpired       -> HttpStatusCode.Unauthorized
}
