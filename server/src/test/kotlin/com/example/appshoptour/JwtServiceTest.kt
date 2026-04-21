package com.example.appshoptour

import com.auth0.jwt.JWT
import com.example.appshoptour.auth.JwtService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class JwtServiceTest {

    @Test fun `access token has 3 parts (header-payload-signature)`() =
        assertEquals(3, JwtService.generateAccessToken("id", "ru").split(".").size)

    @Test fun `token contains userId and lang claims`() {
        val decoded = JWT.decode(JwtService.generateAccessToken("user-1", "es"))
        assertEquals("user-1", decoded.getClaim("userId").asString())
        assertEquals("es", decoded.getClaim("lang").asString())
    }

    @Test fun `verifier accepts generated token`() {
        val token = JwtService.generateAccessToken("user-1", "en")
        JwtService.verifier.verify(token)
    }

    @Test fun `different users get different tokens`() =
        assertNotEquals(
            JwtService.generateAccessToken("user-1", "ru"),
            JwtService.generateAccessToken("user-2", "ru")
        )

    @Test fun `refresh token does not contain lang claim`() {
        val decoded = JWT.decode(JwtService.generateRefreshToken("user-1"))
        assertTrue(decoded.getClaim("lang").isMissing)
    }
}
