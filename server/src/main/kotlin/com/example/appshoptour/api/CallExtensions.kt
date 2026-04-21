package com.example.appshoptour.api

import com.example.appshoptour.auth.AuthError
import com.example.appshoptour.auth.I18n
import com.example.appshoptour.auth.toHttpStatus
import com.example.appshoptour.auth.toMessageKey
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
data class ErrorBody(val error: String)

@Serializable
data class ValidationErrorBody(val error: String, val details: List<String>)

fun ApplicationCall.lang(): I18n.Lang {
    val jwtLang = principal<JWTPrincipal>()?.payload?.getClaim("lang")?.asString()
    return I18n.resolve(
        acceptLanguage = request.headers[HttpHeaders.AcceptLanguage],
        jwtLang = jwtLang
    )
}

suspend fun ApplicationCall.respondError(error: AuthError) {
    val lang = lang()
    val message = I18n.message(error.toMessageKey(), lang)
    if (error is AuthError.ValidationFailed) {
        respond(error.toHttpStatus(), ValidationErrorBody(message, error.fields))
    } else {
        respond(error.toHttpStatus(), ErrorBody(message))
    }
}
