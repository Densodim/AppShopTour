package com.example.appshoptour.presentation.auth

sealed interface AuthUiEvent {
    data object AuthSuccess : AuthUiEvent
}