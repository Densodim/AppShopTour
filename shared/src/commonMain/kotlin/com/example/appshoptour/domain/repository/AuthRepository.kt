package com.example.appshoptour.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String)
    suspend fun register(name: String, email: String, password: String)
}