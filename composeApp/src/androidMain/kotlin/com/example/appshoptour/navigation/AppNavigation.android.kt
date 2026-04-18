package com.example.appshoptour.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.appshoptour.UsersScreen
import com.example.appshoptour.presentation.users.UsersViewModel
import org.koin.compose.koinInject

@Composable
actual fun AppNavigation() {
    val backStack = rememberNavBackStack(UsersRoute)

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<UsersRoute> {
                val viewModel: UsersViewModel = koinInject()
                val state by viewModel.state.collectAsState()
                UsersScreen(
                    state = state,
                    onIntent = viewModel::onIntent
                )
            }
            // Добавляй новые экраны здесь:
            // entry<UserDetailRoute> { route ->
            //     UserDetailScreen(
            //         userId = route.userId,
            //         onBack = { backStack.removeLastOrNull() }
            //     )
            // }
        }
    )
}
