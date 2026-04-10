package com.example.appshoptour

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val jdbcUrl = System.getenv("JDBC_URL")
    val jdbcUser = System.getenv("JDBC_USER")
    val jdbcPassword = System.getenv("JDBC_PASSWORD")

    // Подключаем БД только если переменные окружения заданы (не в тестах)
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
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        get("/health") {
            call.respondText("OK")
        }
    }
}
