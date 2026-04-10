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
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }

    configureDatabase()

    routing {
        get("/health") {
            call.respondText("OK")
        }
        route("/api/v1") {
            usersRoutes()
        }
    }
}

private fun configureDatabase() {
    val jdbcUrl = System.getenv("JDBC_URL")
    val jdbcUser = System.getenv("JDBC_USER")
    val jdbcPassword = System.getenv("JDBC_PASSWORD")

    // Production: PostgreSQL через env vars
    // Local dev: H2 in-memory (не нужно ничего настраивать)
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
