package com.example.appshoptour.database.table


import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

/**
 * Exposed mirror of the `users` table.
 *
 * Flyway should create and evolve the real table in PostgreSQL.
 * This object lets the Kotlin server work with that table safely.
 */
object UsersTable : UUIDTable(name = "users") {
    val name = varchar("name", length = 150)
    val email = varchar("email", length = 255).uniqueIndex()
    val passwordHash = varchar("password_hash", length = 255)
    val preferredCurrency = varchar("preferred_currency", length = 3).default("EUR")
    val preferredLanguage = varchar("preferred_language", length = 10).default("ru")
    val themeMode = varchar("theme_mode", length = 20).default("dark")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
