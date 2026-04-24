package com.example.appshoptour.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute : NavKey

@Serializable
data object AuthRoute : NavKey

@Serializable
data object UsersRoute : NavKey

@Serializable
data class UserDetailRoute(val userId: String) : NavKey
