package com.example.appshoptour.presentation.auth

sealed interface AuthIntent {
    data class NameChanged(val name: String) : AuthIntent
    data class EmailChanged(val email: String) : AuthIntent
    data class PasswordChanged(val password: String) : AuthIntent
    data object Submit : AuthIntent
    data object ToggleMode : AuthIntent
}
