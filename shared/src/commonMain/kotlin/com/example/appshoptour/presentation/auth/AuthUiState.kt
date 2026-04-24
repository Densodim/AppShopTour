package com.example.appshoptour.presentation.auth

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)