package com.example.appshoptour.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import java.util.Date

object JwtService {
    private val secret = System.getenv("JWT_SECRET")
        ?: if (System.getenv("KTOR_ENV") == "production") error("JWT_SECRET not set") else "dev-secret"
    private const val ISSUER = "appshoptour"
    private const val AUDIENCE = "appshoptour-users"
    val ACCESS_TTL = 15 * 60 * 1000L
    val REFRESH_TTL = 30L * 24 * 60 * 60 * 1000L

    val verifier: JWTVerifier = JWT.require(Algorithm.HMAC256(secret))
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateAccessToken(userId: String, lang: String): String =
        buildToken(ACCESS_TTL) {
            withClaim("userId", userId)
            withClaim("lang", lang)
        }

    fun generateRefreshToken(userId: String): String =
        buildToken(REFRESH_TTL) {
            withClaim("userId", userId)
        }

    private fun buildToken(ttl: Long, block: com.auth0.jwt.JWTCreator.Builder.() -> Unit): String =
        JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withExpiresAt(Date(System.currentTimeMillis() + ttl))
            .apply(block)
            .sign(Algorithm.HMAC256(secret))
}
