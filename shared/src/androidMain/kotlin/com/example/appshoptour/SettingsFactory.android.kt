package com.example.appshoptour

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

private lateinit var appContext: Context

fun initSettingsContext(context: Context) {
    appContext = context
}

actual fun createSettings(): Settings {
    val prefs = appContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(prefs)
}
