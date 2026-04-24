package com.example.appshoptour.data.remote

import com.example.appshoptour.data.remote.dto.AuthResponseDto
import com.example.appshoptour.data.remote.dto.LoginRequestDto
import com.example.appshoptour.data.remote.dto.RegisterRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    suspend fun login(email: String, password: String): AuthResponseDto {
        return httpClient.post("$baseUrl/auth/login") {
            setBody(LoginRequestDto(email = email, password = password))
        }.body()
    }

    suspend fun register(name: String, email: String, password: String): AuthResponseDto {
        return httpClient.post("$baseUrl/auth/register") {
            setBody(
                RegisterRequestDto(
                    name = name,
                    email = email,
                    password = password
                )
            )
        }.body()
    }
}