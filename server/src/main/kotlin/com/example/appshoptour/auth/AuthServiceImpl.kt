package com.example.appshoptour.auth

import com.example.appshoptour.api.dto.AuthResponse
import com.example.appshoptour.api.dto.UserResponse
import com.example.appshoptour.database.table.RefreshTokensTable
import com.example.appshoptour.database.table.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.insert
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.UUID

class AuthServiceImpl : AuthService {

    override suspend fun register(name: String, email: String, password: String): AuthResponse =
        newSuspendedTransaction {
            val exists = UsersTable.selectAll()
                .where { UsersTable.email eq email }
                .count() > 0
            if (exists) throw AuthError.EmailAlreadyExists

            val hashed = PasswordHasher.hash(password)
            val now = Instant.now()

            // insertAndGetId — Exposed генерирует UUID автоматически
            val userId = UsersTable.insertAndGetId {
                it[UsersTable.name] = name
                it[UsersTable.email] = email
                it[passwordHash] = hashed.hash
                it[passwordSalt] = hashed.salt
                it[createdAt] = now
                it[updatedAt] = now
            }.value.toString()

            val userRow = UsersTable.selectAll()
                .where { UsersTable.id eq UUID.fromString(userId) }
                .single()

            val userResponse = UserResponse(
                id = userId,
                name = userRow[UsersTable.name],
                email = userRow[UsersTable.email],
                preferredCurrency = userRow[UsersTable.preferredCurrency],
                preferredLanguage = userRow[UsersTable.preferredLanguage],
                themeMode = userRow[UsersTable.themeMode]
            )

            val accessToken = JwtService.generateAccessToken(userId, userResponse.preferredLanguage)
            val refreshToken = JwtService.generateRefreshToken(userId)
            saveRefreshToken(UUID.fromString(userId), refreshToken)

            AuthResponse(accessToken = accessToken, refreshToken = refreshToken, user = userResponse)
        }

    override suspend fun login(email: String, password: String): AuthResponse =
        newSuspendedTransaction {
            val row = UsersTable.selectAll()
                .where { UsersTable.email eq email }
                .singleOrNull()
                ?: throw AuthError.InvalidCredentials

            val storedHash = row[UsersTable.passwordHash]
            val storedSalt = row[UsersTable.passwordSalt]

            if (storedSalt == null || !PasswordHasher.verify(password, storedHash, storedSalt)) {
                throw AuthError.InvalidCredentials
            }

            val userId = row[UsersTable.id].value.toString()
            val lang = row[UsersTable.preferredLanguage]
            val accessToken = JwtService.generateAccessToken(userId, lang)
            val refreshToken = JwtService.generateRefreshToken(userId)
            saveRefreshToken(UUID.fromString(userId), refreshToken)

            AuthResponse(accessToken = accessToken, refreshToken = refreshToken)
        }

    override suspend fun refresh(refreshToken: String): AuthResponse =
        newSuspendedTransaction {
            val tokenHash = hashToken(refreshToken)
            val now = Instant.now()

            val row = RefreshTokensTable.selectAll()
                .where { RefreshTokensTable.tokenHash eq tokenHash }
                .singleOrNull()
                ?: throw AuthError.TokenInvalid

            if (row[RefreshTokensTable.revoked]) throw AuthError.TokenInvalid
            if (row[RefreshTokensTable.expiresAt].isBefore(now)) throw AuthError.TokenExpired

            RefreshTokensTable.update({ RefreshTokensTable.tokenHash eq tokenHash }) {
                it[revoked] = true
            }

            val userId = row[RefreshTokensTable.userId].value.toString()
            val userRow = UsersTable.selectAll()
                .where { UsersTable.id eq UUID.fromString(userId) }
                .single()

            val newAccess = JwtService.generateAccessToken(userId, userRow[UsersTable.preferredLanguage])
            val newRefresh = JwtService.generateRefreshToken(userId)
            saveRefreshToken(UUID.fromString(userId), newRefresh)

            AuthResponse(accessToken = newAccess, refreshToken = newRefresh)
        }

    override suspend fun logout(userId: String): Unit =
        newSuspendedTransaction {
            RefreshTokensTable.update({
                RefreshTokensTable.userId eq EntityID(UUID.fromString(userId), UsersTable)
            }) {
                it[revoked] = true
            }
        }

    private fun saveRefreshToken(userId: UUID, token: String) {
        RefreshTokensTable.insert {
            it[RefreshTokensTable.userId] = EntityID(userId, UsersTable)
            it[tokenHash] = hashToken(token)
            it[expiresAt] = Instant.now().plusMillis(JwtService.REFRESH_TTL)
            it[createdAt] = Instant.now()
        }
    }

    private fun hashToken(token: String): String =
        Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(token.encodeToByteArray())
        )
}
