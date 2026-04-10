package com.example.appshoptour.data.repository

import com.example.appshoptour.data.remote.UserApiClient
import com.example.appshoptour.data.remote.dto.toDomain
import com.example.appshoptour.domain.model.User
import com.example.appshoptour.domain.repository.UserRepository

/**
 * Реализация репозитория — в data слое.
 * Знает про API клиент (remote), но не про ViewModel или Use Case.
 * В будущем добавим локальный кеш (SQLDelight DAO) и Cache-First стратегию.
 */
class UserRepositoryImpl(
    private val apiClient: UserApiClient
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> =
        apiClient.getUsers().map { list -> list.map { it.toDomain() } }

    override suspend fun getUserById(id: String): Result<User> =
        apiClient.getUserById(id).map { it.toDomain() }
}
