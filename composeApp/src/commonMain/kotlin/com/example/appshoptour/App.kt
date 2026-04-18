package com.example.appshoptour

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appshoptour.domain.model.UiError
import com.example.appshoptour.domain.model.User
import com.example.appshoptour.navigation.AppNavigation
import com.example.appshoptour.presentation.users.UsersIntent
import com.example.appshoptour.presentation.users.UsersUiState

@Composable
fun App() {
    AppTheme {
        AppNavigation()
    }
}

@Composable
fun UsersScreen(
    state: UsersUiState,
    onIntent: (UsersIntent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            state.error != null -> ErrorView(
                error = state.error!!,
                onRetry = { onIntent(UsersIntent.Refresh) },
                modifier = Modifier.align(Alignment.Center)
            )
            state.showEmpty -> EmptyView(Modifier.align(Alignment.TopStart))
            else -> UsersList(
                users = state.users,
                onUserClick = { onIntent(UsersIntent.SelectUser(it.id)) }
            )
        }
    }
}

@Composable
private fun UsersList(users: List<User>, onUserClick: (User) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(users, key = { it.id }) { user ->
            UserCard(user = user, onClick = { onUserClick(user) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun UserCard(user: User, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = user.name, style = MaterialTheme.typography.titleMedium)
            Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ErrorView(error: UiError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = error.title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = error.message, style = MaterialTheme.typography.bodyMedium)
        if (error.isRetryable) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Повторить") }
        }
    }
}

@Composable
private fun EmptyView(modifier: Modifier = Modifier) {
    Text(
        text = "Пользователей пока нет",
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge
    )
}
