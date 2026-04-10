package com.example.appshoptour

import android.app.Application
import com.example.appshoptour.di.sharedModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application класс — точка входа для инициализации Koin.
 * Не забудь добавить android:name=".MainApplication" в AndroidManifest.xml
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            androidLogger(Level.DEBUG)
            modules(sharedModules(BuildConfig.BASE_URL))
        }
    }
}
