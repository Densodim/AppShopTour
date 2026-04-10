package com.example.appshoptour.presentation.users

import com.example.appshoptour.domain.model.AppError
import com.example.appshoptour.domain.model.UiError
import com.example.appshoptour.domain.model.User
import com.example.appshoptour.domain.usecase.GetUsersUseCase
import com.example.appshoptour.presentation.base.BaseViewModel
import com.example.appshoptour.presentation.mapper.toUiError
import kotlinx.coroutines.launch

// ---------- State ----------

/**
 * Полное состояние экрана пользователей.
 * data class + copy() = иммутабельное обновление состояния.
 */
data class UsersUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiError? = null
) {
    val showEmpty: Boolean get() = !isLoading && error == null && users.isEmpty()
    val showContent: Boolean get() = !isLoading && error == null && users.isNotEmpty()
}

// ---------- Intent ----------

/**
 * Намерения пользователя (User Actions → ViewModel).
 * UDF: события идут ВВЕРХ — в ViewModel, состояние идёт ВНИЗ — в UI.
 */
sealed interface UsersIntent {
    data object Refresh : UsersIntent
    data class SelectUser(val userId: String) : UsersIntent
}

// ---------- Event ----------

/**
 * Одноразовые события (навигация, snackbar) — НЕ хранятся в state.
 */
sealed interface UsersUiEvent {
    data class NavigateToDetail(val userId: String) : UsersUiEvent
    data class ShowSnackbar(val message: String) : UsersUiEvent
}

// ---------- ViewModel ----------

class UsersViewModel(
    private val getUsersUseCase: GetUsersUseCase
) : BaseViewModel<UsersUiState, UsersUiEvent>(UsersUiState()) {

    init {
        loadUsers()
    }

    fun onIntent(intent: UsersIntent) {
        when (intent) {
            is UsersIntent.Refresh -> loadUsers()
            is UsersIntent.SelectUser -> emitEvent(UsersUiEvent.NavigateToDetail(intent.userId))
        }
    }

    private fun loadUsers() {
        launch {
            updateState { copy(isLoading = true, error = null) }
            getUsersUseCase()
                .onSuccess { users ->
                    updateState { copy(users = users, isLoading = false) }
                }
                .onFailure { throwable ->
                    val appError = (throwable as? AppError) ?: AppError.Unknown(throwable)
                    updateState { copy(error = appError.toUiError(), isLoading = false) }
                }
        }
    }
}
