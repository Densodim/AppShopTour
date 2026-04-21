package com.example.appshoptour.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class RefreshResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)

class AuthInterceptor(
    private val tokenStorage: TokenStorage,
    private val baseUrl: String
) {
    fun install(client: HttpClient) {
        client.plugin(HttpSend).intercept { request ->
            val originalCall = execute(request)

            // Не перехватываем auth эндпоинты — защита от бесконечной рекурсии
            if (originalCall.response.status != HttpStatusCode.Unauthorized
                || "auth" in request.url.pathSegments
            ) {
                return@intercept originalCall
            }

            val refreshToken = tokenStorage.getRefreshToken()
                ?: return@intercept originalCall

            val refreshed = tryRefresh(client, refreshToken)
                ?: return@intercept originalCall

            tokenStorage.saveTokens(refreshed.accessToken, refreshed.refreshToken)
            request.headers[HttpHeaders.Authorization] = "Bearer ${refreshed.accessToken}"
            execute(request)
        }
    }

    private suspend fun tryRefresh(client: HttpClient, refreshToken: String): RefreshResponse? =
        runCatching {
            client.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refresh_token" to refreshToken))
            }.body<RefreshResponse>()
        }.getOrNull()
}
