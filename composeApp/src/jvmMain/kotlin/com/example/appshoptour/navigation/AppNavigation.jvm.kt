package com.example.appshoptour.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.appshoptour.UsersScreen
import com.example.appshoptour.domain.preferences.OnboardingPreferences
import com.example.appshoptour.presentation.auth.AuthUiEvent
import com.example.appshoptour.presentation.auth.AuthViewModel
import com.example.appshoptour.presentation.users.UsersUiEvent
import com.example.appshoptour.presentation.users.UsersViewModel
import com.example.appshoptour.ui.auth.AuthScreen
import com.example.appshoptour.ui.onboarding.OnboardingScreen
import com.example.appshoptour.ui.userdetail.UserDetailScreen
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

// Desktop JVM — простая навигация без Navigation3 UI
// (Navigation3 UI тянет Android-специфичные зависимости)
@Composable
actual fun AppNavigation() {
    val usersViewModel: UsersViewModel = koinInject()
    val authViewModel: AuthViewModel = koinInject()
    val onboardingPreferences: OnboardingPreferences = koinInject()
    val usersState by usersViewModel.state.collectAsState()
    val authState by authViewModel.state.collectAsState()

    var hasSeenOnboarding by rememberSaveable { mutableStateOf(onboardingPreferences.isCompleted()) }
    var isAuthorized by rememberSaveable { mutableStateOf(false) }
    var selectedUserId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(usersViewModel) {
        usersViewModel.events.collectLatest { event ->
            when (event) {
                is UsersUiEvent.NavigateToDetail -> selectedUserId = event.userId
                is UsersUiEvent.ShowSnackbar -> Unit
            }
        }
    }

    LaunchedEffect(authViewModel) {
        authViewModel.events.collectLatest { event ->
            when (event) {
                AuthUiEvent.AuthSuccess -> isAuthorized = true
            }
        }
    }

    when {
        !hasSeenOnboarding -> {
            OnboardingScreen(
                onContinue = {
                    onboardingPreferences.setCompleted(true)
                    hasSeenOnboarding = true
                }
            )
        }
        !isAuthorized -> {
            AuthScreen(
                state = authState,
                onIntent = authViewModel::onInstant
            )
        }
        selectedUserId == null -> {
            UsersScreen(
                state = usersState,
                onIntent = usersViewModel::onIntent
            )
        }
        else -> {
            UserDetailScreen(
                userId = selectedUserId!!,
                onBack = { selectedUserId = null }
            )
        }
    }
}
