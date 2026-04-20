package com.example.appshoptour.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.appshoptour.UsersScreen
import com.example.appshoptour.presentation.users.UsersUiEvent
import com.example.appshoptour.presentation.users.UsersViewModel
import com.example.appshoptour.ui.userdetail.UserDetailScreen
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
actual fun AppNavigation() {
    val backStack = rememberNavBackStack(UsersRoute)
    val viewModel: UsersViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UsersUiEvent.NavigateToDetail -> backStack.add(UserDetailRoute(event.userId))
                is UsersUiEvent.ShowSnackbar -> Unit
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<UsersRoute> {
                UsersScreen(
                    state = state,
                    onIntent = viewModel::onIntent
                )
            }

            entry<UserDetailRoute> { route ->
                UserDetailScreen(
                    userId = route.userId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
