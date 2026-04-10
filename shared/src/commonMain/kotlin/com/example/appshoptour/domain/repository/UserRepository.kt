package com.example.appshoptour.domain.repository

import com.example.appshoptour.domain.model.User

/**
 * Интерфейс репозитория — только в domain слое.
 * Реализация — в data слое. Это инверсия зависимостей (SOLID-D).
 */
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: String): Result<User>
}
