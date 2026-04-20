package com.example.appshoptour.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.appshoptour.UsersScreen
import com.example.appshoptour.presentation.users.UsersUiEvent
import com.example.appshoptour.presentation.users.UsersViewModel
import com.example.appshoptour.ui.userdetail.UserDetailScreen
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

// Desktop JVM — простая навигация без Navigation3 UI
// (Navigation3 UI тянет Android-специфичные зависимости)
@Composable
actual fun AppNavigation() {
    val viewModel: UsersViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    var selectedUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UsersUiEvent.NavigateToDetail -> selectedUserId = event.userId
                is UsersUiEvent.ShowSnackbar -> Unit
            }
        }
    }

    if (selectedUserId == null) {
        UsersScreen(
            state = state,
            onIntent = viewModel::onIntent
        )
    } else {
        UserDetailScreen(
            userId = selectedUserId!!,
            onBack = { selectedUserId = null }
        )
    }
}
