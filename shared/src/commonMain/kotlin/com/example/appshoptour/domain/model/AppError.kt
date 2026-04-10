package com.example.appshoptour.domain.model

/**
 * Иерархия ошибок приложения.
 * Все ошибки сводятся к одному из этих типов — нет неожиданных Exception в UI.
 */
sealed class AppError : Exception() {
    // Сетевые ошибки
    data object Network : AppError()
    data object Timeout : AppError()
    data object Unauthorized : AppError()
    data object NotFound : AppError()
    data class Api(val code: Int, override val message: String?) : AppError()
    data class Server(val code: Int) : AppError()
    data class Validation(override val message: String?) : AppError()

    // Локальные ошибки
    data object DatabaseError : AppError()

    // Неизвестная ошибка — оборачивает любой Throwable
    data class Unknown(val origin: Throwable) : AppError()
}
