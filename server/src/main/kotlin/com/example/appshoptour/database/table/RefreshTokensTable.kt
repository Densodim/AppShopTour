package com.example.appshoptour.database.table

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.timestamp

object RefreshTokensTable : UUIDTable(name = "refresh_tokens") {
    val userId = reference("user_id", UsersTable)
    val tokenHash = varchar("token_hash", length = 255).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")
    val revoked = bool("revoked").default(false)
}
