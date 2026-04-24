package com.example.appshoptour.data.remote

import com.russhwolf.settings.Settings

class TokenStorageImpl(
    private val settings: Settings
) : TokenStorage {

    override suspend fun getAccessToken(): String? {
        return settings.getStringOrNull(KEY_ACCESS_TOKEN)
    }

    override suspend fun getRefreshToken(): String? {
        return settings.getStringOrNull(KEY_REFRESH_TOKEN)
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        settings.putString(KEY_ACCESS_TOKEN, accessToken)
        settings.putString(KEY_REFRESH_TOKEN, refreshToken)
    }

    override suspend fun clear() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
