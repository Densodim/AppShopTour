package com.example.appshoptour

import com.example.appshoptour.api.usersRoutes
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // JSON сериализация для всех ответов
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }

    val jdbcUrl = System.getenv("JDBC_URL")
    val jdbcUser = System.getenv("JDBC_USER")
    val jdbcPassword = System.getenv("JDBC_PASSWORD")

    if (jdbcUrl != null && jdbcUser != null && jdbcPassword != null) {
        val dataSource = HikariDataSource(HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = jdbcUser
            this.password = jdbcPassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
        })

        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        Database.connect(dataSource)
    }

    routing {
        get("/health") {
            call.respondText("OK")
        }

        // Все API роуты под /api/v1/
        route("/api/v1") {
            usersRoutes()
        }
    }
}
