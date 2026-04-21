package com.example.appshoptour

import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.assertEquals

abstract class AuthTestBase {

    protected fun authTest(block: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application { module() }
            block()
        }

    protected suspend fun ApplicationTestBuilder.registerUser(
        email: String = "test@test.com",
        password: String = "password123",
        name: String = "Test User",
        lang: String = "en"
    ): String {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.AcceptLanguage, lang)
            setBody("""{"name":"$name","email":"$email","password":"$password"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        return body["access_token"]!!.jsonPrimitive.content
    }
}
