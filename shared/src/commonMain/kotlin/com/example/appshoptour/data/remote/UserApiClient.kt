package com.example.appshoptour.data.remote

import com.example.appshoptour.data.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType

private const val BASE_URL = "http://localhost:8080/api/v1"

/**
 * API клиент для работы с /api/v1/users.
 * Каждый метод возвращает Result<T> — нет throws, нет необработанных исключений.
 */
class UserApiClient(private val client: HttpClient) {

    suspend fun getUsers(): Result<List<UserDto>> = runCatching {
        client.get("$BASE_URL/users") {
            accept(ContentType.Application.Json)
        }.body()
    }

    suspend fun getUserById(id: String): Result<UserDto> = runCatching {
        client.get("$BASE_URL/users/$id") {
            accept(ContentType.Application.Json)
        }.body()
    }
}
