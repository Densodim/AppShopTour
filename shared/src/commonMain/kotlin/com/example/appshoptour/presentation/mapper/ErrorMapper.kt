package com.example.appshoptour.presentation.mapper

import com.example.appshoptour.domain.model.AppError
import com.example.appshoptour.domain.model.UiError
import com.example.appshoptour.domain.model.UiErrorAction

/**
 * Маппер AppError -> UiError.
 * Техническая ошибка превращается в понятное пользователю сообщение.
 */
fun AppError.toUiError(): UiError = when (this) {
    AppError.Network -> UiError(
        title = "Нет соединения",
        message = "Проверьте подключение к интернету и попробуйте снова",
        isRetryable = true
    )
    AppError.Timeout -> UiError(
        title = "Время ожидания истекло",
        message = "Сервер не ответил. Попробуйте позже",
        isRetryable = true
    )
    AppError.Unauthorized -> UiError(
        title = "Сессия истекла",
        message = "Войдите в аккаунт снова",
        isRetryable = false,
        action = UiErrorAction.Logout
    )
    AppError.NotFound -> UiError(
        title = "Не найдено",
        message = "Запрашиваемые данные не существуют",
        isRetryable = false
    )
    is AppError.Server -> UiError(
        title = "Ошибка сервера",
        message = "Что-то пошло не так на нашей стороне. Попробуйте позже",
        isRetryable = true
    )
    is AppError.Validation -> UiError(
        title = "Ошибка валидации",
        message = this.message ?: "Некорректные данные",
        isRetryable = false
    )
    AppError.DatabaseError -> UiError(
        title = "Ошибка базы данных",
        message = "Не удалось сохранить данные",
        isRetryable = true
    )
    else -> UiError(
        title = "Что-то пошло не так",
        message = "Неожиданная ошибка. Попробуйте снова",
        isRetryable = true
    )
}

/**
 * Railway-oriented programming: цепочка Result без вложенных try-catch.
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    fold(onSuccess = { transform(it) }, onFailure = { Result.failure(it) })

inline fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
    fold(onSuccess = { this }, onFailure = { Result.failure(transform(it)) })
