package com.example.appshoptour

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRouteTest : AuthTestBase() {

    @Test fun `register returns 201 with tokens`() = authTest {
        val token = registerUser()
        assertTrue(token.isNotBlank())
    }

    @Test fun `register returns 409 on duplicate email`() = authTest {
        registerUser(email = "dup@test.com")
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"X","email":"dup@test.com","password":"password123"}""")
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test fun `register validation error localized in Russian`() = authTest {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.AcceptLanguage, "ru")
            setBody("""{"name":"","email":"bad","password":"1"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Ошибка валидации"))
    }

    @Test fun `register validation error localized in Spanish`() = authTest {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.AcceptLanguage, "es")
            setBody("""{"name":"","email":"bad","password":"1"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Error de validación"))
    }

    @Test fun `login returns same error for wrong email and wrong password (anti-enumeration)`() = authTest {
        registerUser(email = "user@test.com")
        val wrongEmail = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"nobody@test.com","password":"pass"}""")
        }
        val wrongPass = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"user@test.com","password":"wrongpass"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, wrongEmail.status)
        assertEquals(HttpStatusCode.Unauthorized, wrongPass.status)
        assertEquals(wrongEmail.bodyAsText(), wrongPass.bodyAsText())
    }

    @Test fun `protected route returns 401 without token`() = authTest {
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/users").status)
    }

    @Test fun `protected route returns 200 with valid token`() = authTest {
        val token = registerUser(email = "auth@test.com")
        val response = client.get("/api/v1/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test fun `internal error does not leak stack trace`() = authTest {
        val response = client.get("/api/v1/test-error")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
