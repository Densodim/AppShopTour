package com.example.appshoptour

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings

actual fun createSettings(): Settings = NSUserDefaultsSettings.Factory().create("app_settings")
