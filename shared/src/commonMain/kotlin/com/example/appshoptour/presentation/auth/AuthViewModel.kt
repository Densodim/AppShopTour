package com.example.appshoptour.presentation.auth

import com.example.appshoptour.domain.repository.AuthRepository
import com.example.appshoptour.presentation.base.BaseViewModel
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthUiState, AuthUiEvent>(AuthUiState()) {
    fun onInstant(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.NameChanged -> updateState {
                copy(name = intent.name, errorMessage = null)
            }

            is AuthIntent.EmailChanged -> updateState {
                copy(email = intent.email, errorMessage = null)
            }

            is AuthIntent.PasswordChanged -> updateState {
                copy(password = intent.password, errorMessage = null)
            }

            AuthIntent.ToggleMode -> updateState {
                copy(
                    isRegisterMode = !isRegisterMode,
                    errorMessage = null
                )
            }

            AuthIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = state.value

        if (state.email.isBlank() || state.password.isBlank()) {
            updateState {
                copy(errorMessage = "Email and password are required")
            }
            return
        }
        if (state.isRegisterMode && state.name.isBlank()) {
            updateState {
                copy(errorMessage = "Name is required")
            }
            return
        }

        launch {
            updateState { copy(isLoading = true, errorMessage = null) }

            runCatching {
                if (state.isRegisterMode) {
                    authRepository.register(
                        name = state.name,
                        email = state.email,
                        password = state.password
                    )
                } else {
                    authRepository.login(
                        email = state.email,
                        password = state.password
                    )
                }
            }.onSuccess {
                updateState { copy(isLoading = false) }
                emitEvent(AuthUiEvent.AuthSuccess)
            }.onFailure {
                error ->
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unknown error"
                    )
                }
            }
        }
    }
}