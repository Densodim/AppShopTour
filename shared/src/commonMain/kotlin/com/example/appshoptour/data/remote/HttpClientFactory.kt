package com.example.appshoptour.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Фабрика HTTP клиента.
 * HttpClient() без параметра движка — Ktor автоматически выбирает
 * движок по classpath: OkHttp (Android), Darwin (iOS), CIO (JVM), Js (Web).
 */
fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            explicitNulls = false
        })
    }

    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO  // NONE в релизе, INFO/HEADERS для отладки
    }
}
