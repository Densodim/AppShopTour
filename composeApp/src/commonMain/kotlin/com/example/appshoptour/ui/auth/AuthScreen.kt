package com.example.appshoptour.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appshoptour.presentation.auth.AuthIntent
import com.example.appshoptour.presentation.auth.AuthUiState

@Composable
fun AuthScreen(
    state: AuthUiState,
    onIntent: (AuthIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (state.isRegisterMode) "Регистрация" else "Вход",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isRegisterMode) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onIntent(AuthIntent.NameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Имя") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = { onIntent(AuthIntent.EmailChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { onIntent(AuthIntent.PasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Пароль") },
            singleLine = true
        )

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onIntent(AuthIntent.Submit) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(if (state.isRegisterMode) "Зарегистрироваться" else "Войти")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onIntent(AuthIntent.ToggleMode) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (state.isRegisterMode) {
                    "У меня уже есть аккаунт"
                } else {
                    "Создать аккаунт"
                }
            )
        }
    }
}