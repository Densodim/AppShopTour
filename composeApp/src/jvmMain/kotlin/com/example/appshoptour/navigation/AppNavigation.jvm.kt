package com.example.appshoptour.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.appshoptour.UsersScreen
import com.example.appshoptour.presentation.users.UsersViewModel
import org.koin.compose.koinInject

// Desktop JVM — простая навигация без Navigation3 UI
// (Navigation3 UI тянет Android-специфичные зависимости)
@Composable
actual fun AppNavigation() {
    val viewModel: UsersViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    UsersScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}
