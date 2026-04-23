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
import com.example.appshoptour.presentation.users.UsersUiEvent
import com.example.appshoptour.presentation.users.UsersViewModel
import com.example.appshoptour.ui.navigationBar.AppNavigationBar
import com.example.appshoptour.ui.navigationBar.TopLevelDestination
import com.example.appshoptour.ui.onboarding.OnboardingScreen
import com.example.appshoptour.ui.userdetail.UserDetailScreen
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
actual fun AppNavigation() {
    val onboardingPreferences: OnboardingPreferences = koinInject()
    // Временный учебный флаг.
    var hasSeenOnboarding by rememberSaveable { mutableStateOf(onboardingPreferences.isCompleted()) }

    // Выбираем стартовый экран один раз при создании navigation state.
    val startDestination = if (hasSeenOnboarding) {
        UsersRoute
    }else{
        OnboardingRoute
    }

    val backStack = rememberNavBackStack(startDestination)
    val currentRoute = backStack.lastOrNull()

    val viewModel: UsersViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    val currentDestination = when (currentRoute) {
        is UsersRoute -> TopLevelDestination.Users
        else -> TopLevelDestination.Users
    }

    val showBottomBar = currentRoute is UsersRoute || currentRoute is UserDetailRoute

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UsersUiEvent.NavigateToDetail -> backStack.add(UserDetailRoute(event.userId))
                is UsersUiEvent.ShowSnackbar -> Unit
            }
        }
    }

    // Scaffold — это "каркас" экрана с поддержкой bottomBar, topBar и т.д.
    Scaffold(
        bottomBar = {
            if (showBottomBar){
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
        // innerPadding — это отступ, который Scaffold автоматически
        // рассчитывает чтобы контент не перекрывался bottomBar'ом
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {
                entry<OnboardingRoute> {
                    OnboardingScreen (
                        onContinue = {
                            onboardingPreferences.setCompleted(true)
                            hasSeenOnboarding = true
                            backStack.clear()
                            backStack.add(UsersRoute)
                        }
                    )
                }
                entry<UsersRoute> { UsersScreen(state, viewModel::onIntent) }
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
