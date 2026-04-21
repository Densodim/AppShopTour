package com.example.appshoptour.api

import com.example.appshoptour.api.dto.UserResponse
import com.example.appshoptour.database.table.UsersTable
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

fun Route.usersRoutes() {
    authenticate("auth-jwt") {
        route("/users") {

            get {
                val users = transaction {
                    UsersTable.selectAll().map { row ->
                        UserResponse(
                            id = row[UsersTable.id].value.toString(),
                            name = row[UsersTable.name],
                            email = row[UsersTable.email],
                            preferredCurrency = row[UsersTable.preferredCurrency],
                            preferredLanguage = row[UsersTable.preferredLanguage],
                            themeMode = row[UsersTable.themeMode]
                        )
                    }
                }
                call.respond(users)
            }

            get("/{id}") {
                val rawId = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id")

                val uuid = runCatching { UUID.fromString(rawId) }.getOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")

                val user = transaction {
                    UsersTable.selectAll()
                        .where { UsersTable.id eq uuid }
                        .singleOrNull()
                        ?.let { row ->
                            UserResponse(
                                id = row[UsersTable.id].value.toString(),
                                name = row[UsersTable.name],
                                email = row[UsersTable.email],
                                preferredCurrency = row[UsersTable.preferredCurrency],
                                preferredLanguage = row[UsersTable.preferredLanguage],
                                themeMode = row[UsersTable.themeMode]
                            )
                        }
                }

                if (user == null) call.respond(HttpStatusCode.NotFound, "User not found")
                else call.respond(user)
            }
        }
    }
}
