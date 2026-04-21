package com.example.appshoptour.api

import com.example.appshoptour.api.dto.LoginRequest
import com.example.appshoptour.api.dto.RefreshRequest
import com.example.appshoptour.api.dto.RegisterRequest
import com.example.appshoptour.auth.AuthError
import com.example.appshoptour.auth.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        rateLimit(RateLimitName("auth")) {
            post("/register") {
                val req = call.receive<RegisterRequest>()
                val errors = req.validate()
                if (errors.isNotEmpty()) throw AuthError.ValidationFailed(errors)
                call.respond(HttpStatusCode.Created, authService.register(req.name, req.email, req.password))
            }

            post("/login") {
                val req = call.receive<LoginRequest>()
                call.respond(authService.login(req.email, req.password))
            }

            post("/refresh") {
                val req = call.receive<RefreshRequest>()
                call.respond(authService.refresh(req.refreshToken))
            }
        }

        authenticate("auth-jwt") {
            post("/logout") {
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asString()
                authService.logout(userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
