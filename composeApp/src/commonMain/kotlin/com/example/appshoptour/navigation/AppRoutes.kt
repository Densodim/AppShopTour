package com.example.appshoptour.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Все маршруты должны реализовывать NavKey — маркер для Navigation3
@Serializable
data object UsersRoute : NavKey

// Добавляй новые экраны сюда:
// @Serializable data object HomeRoute : NavKey
// @Serializable data object CatalogRoute : NavKey
 @Serializable
 data class UserDetailRoute(val userId: String) : NavKey
