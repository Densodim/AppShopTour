package com.example.appshoptour.ui.navigationBar

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar as MaterialNavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Нижняя панель навигации для top-level экранов приложения.
 *
 * Важно: этот composable НЕ управляет back stack сам.
 * Он только рисует UI и сообщает наверх, какой раздел выбрали.
 */
@Composable
fun AppNavigationBar(
    selectedDestination: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    MaterialNavigationBar(modifier = modifier) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Text(text = destination.shortLabel)
                },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}

/**
 * Top-level разделы, которые можно показывать в bottom bar.
 *
 * Сейчас у нас только экран пользователей, но структура уже готова
 * для добавления новых разделов.
 */
enum class TopLevelDestination(
    val label: String,
    val contentDescription: String,
    val shortLabel: String
) {
    Users(
        label = "Users",
        contentDescription = "Open users screen",
        shortLabel = "U"
    )
}
