package com.example.appshoptour.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.appshoptour.UsersScreen
import com.example.appshoptour.domain.preferences.OnboardingPreferences
import com.example.appshoptour.presentation.auth.AuthUiEvent
import com.example.appshoptour.presentation.auth.AuthViewModel
import com.example.appshoptour.presentation.users.UsersUiEvent
import com.example.appshoptour.presentation.users.UsersViewModel
import com.example.appshoptour.ui.auth.AuthScreen
import com.example.appshoptour.ui.navigationBar.AppNavigationBar
import com.example.appshoptour.ui.navigationBar.TopLevelDestination
import com.example.appshoptour.ui.onboarding.OnboardingScreen
import com.example.appshoptour.ui.userdetail.UserDetailScreen
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
actual fun AppNavigation() {
    val usersViewModel: UsersViewModel = koinInject()
    val authViewModel: AuthViewModel = koinInject()
    val onboardingPreferences: OnboardingPreferences = koinInject()
    val usersState by usersViewModel.state.collectAsState()
    val authState by authViewModel.state.collectAsState()

    var hasSeenOnboarding by rememberSaveable {
        mutableStateOf(onboardingPreferences.isCompleted())
    }

    val startDestination = if (hasSeenOnboarding) AuthRoute else OnboardingRoute
    val backStack = rememberNavBackStack(startDestination)
    val currentRoute = backStack.lastOrNull()
    val showBottomBar = currentRoute is UsersRoute
    val currentDestination = TopLevelDestination.Users

    LaunchedEffect(usersViewModel) {
        usersViewModel.events.collectLatest { event ->
            when (event) {
                is UsersUiEvent.NavigateToDetail -> backStack.add(UserDetailRoute(event.userId))
                is UsersUiEvent.ShowSnackbar -> Unit
            }
        }
    }

    LaunchedEffect(authViewModel) {
        authViewModel.events.collectLatest { event ->
            when (event) {
                AuthUiEvent.AuthSuccess -> {
                    backStack.clear()
                    backStack.add(UsersRoute)
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppNavigationBar(
                    selectedDestination = currentDestination,
                    onDestinationSelected = { destination ->
                        when (destination) {
                            TopLevelDestination.Users -> {
                                backStack.clear()
                                backStack.add(UsersRoute)
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {
                entry<OnboardingRoute> {
                    OnboardingScreen(
                        onContinue = {
                            onboardingPreferences.setCompleted(true)
                            hasSeenOnboarding = true

                            backStack.clear()
                            backStack.add(AuthRoute)
                        }
                    )
                }

                entry<AuthRoute> {
                    AuthScreen(
                        state = authState,
                        onIntent = authViewModel::onInstant
                    )
                }

                entry<UsersRoute> {
                    UsersScreen(
                        state = usersState,
                        onIntent = usersViewModel::onIntent
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
}
