package com.example.appshoptour.data.remote

import com.example.appshoptour.data.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType

/**
 * API клиент для работы с /api/v1/users.
 * baseUrl приходит через Koin из BuildConfig.BASE_URL (debug/release разные значения).
 */
class UserApiClient(private val client: HttpClient, private val baseUrl: String) {

    suspend fun getUsers(): Result<List<UserDto>> = runCatching {
        client.get("$baseUrl/users") {
            accept(ContentType.Application.Json)
        }.body()
    }

    suspend fun getUserById(id: String): Result<UserDto> = runCatching {
        client.get("$baseUrl/users/$id") {
            accept(ContentType.Application.Json)
        }.body()
    }
}
