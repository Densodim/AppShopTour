package com.example.appshoptour.domain.model

/**
 * Модель ошибки для отображения в UI.
 * Содержит только то, что нужно экрану — без технических деталей.
 */
data class UiError(
    val title: String,
    val message: String,
    val isRetryable: Boolean,
    val action: UiErrorAction? = null
)

sealed interface UiErrorAction {
    data object Logout : UiErrorAction
    data class Navigate(val route: String) : UiErrorAction
}
