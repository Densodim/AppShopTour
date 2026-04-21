package com.example.appshoptour

import com.example.appshoptour.api.authRoutes
import com.example.appshoptour.api.respondError
import com.example.appshoptour.api.usersRoutes
import com.example.appshoptour.auth.AuthError
import com.example.appshoptour.auth.AuthServiceImpl
import com.example.appshoptour.auth.I18n
import com.example.appshoptour.auth.JwtService
import com.example.appshoptour.api.lang
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import kotlin.time.Duration.Companion.minutes

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }

    install(StatusPages) {
        exception<AuthError> { call, error ->
            call.respondError(error)
        }
        status(HttpStatusCode.TooManyRequests) { call, _ ->
            val lang = call.lang()
            call.respond(
                HttpStatusCode.TooManyRequests,
                mapOf("error" to I18n.message(I18n.MessageKey.RATE_LIMIT_EXCEEDED, lang))
            )
        }
        exception<Throwable> { call, _ ->
            val lang = call.lang()
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to I18n.message(I18n.MessageKey.INTERNAL_ERROR, lang))
            )
        }
    }

    install(RateLimit) {
        register(RateLimitName("auth")) {
            rateLimiter(limit = 5, refillPeriod = 1.minutes)
            requestKey { call -> call.request.local.remoteHost }
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtService.verifier)
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null)
                    JWTPrincipal(credential.payload)
                else null
            }
            challenge { _, _ ->
                val lang = call.lang()
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to I18n.message(I18n.MessageKey.TOKEN_INVALID, lang))
                )
            }
        }
    }

    configureDatabase()

    val authService = AuthServiceImpl()

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")

        get("/health") {
            call.respondText("OK")
        }
        route("/api/v1") {
            authRoutes(authService)
            usersRoutes()
        }
    }
}

private fun configureDatabase() {
    val jdbcUrl = System.getenv("JDBC_URL")
    val jdbcUser = System.getenv("JDBC_USER")
    val jdbcPassword = System.getenv("JDBC_PASSWORD")

    val (url, user, password, driver) = if (jdbcUrl != null && jdbcUser != null && jdbcPassword != null) {
        listOf(jdbcUrl, jdbcUser, jdbcPassword, "org.postgresql.Driver")
    } else {
        listOf(
            "jdbc:h2:mem:appshoptour;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "sa", "", "org.h2.Driver"
        )
    }

    val dataSource = HikariDataSource(HikariConfig().apply {
        this.jdbcUrl = url
        this.username = user
        this.password = password
        driverClassName = driver
        maximumPoolSize = 10
    })

    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate()

    Database.connect(dataSource)
}
