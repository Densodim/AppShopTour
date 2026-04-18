package com.example.appshoptour

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.appshoptour.ui.theme.DarkColorScheme
import com.example.appshoptour.ui.theme.LightColorScheme
import com.example.appshoptour.ui.theme.appTypography

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = appTypography(),
        content     = content
    )
}
