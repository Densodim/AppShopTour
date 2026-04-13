package com.example.appshoptour

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.appshoptour.di.sharedModules
import org.koin.core.context.startKoin

private const val DESKTOP_BASE_URL = "http://91.84.122.246:8080/api/v1"

fun main() {
    startKoin {
        modules(sharedModules(DESKTOP_BASE_URL))
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "AppShopTour",
        ) {
            App()
        }
    }
}