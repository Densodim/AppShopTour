package com.example.appshoptour.data.preferences

import com.example.appshoptour.domain.preferences.OnboardingPreferences
import com.russhwolf.settings.Settings

class OnboardingPreferencesImpl(
    private val setting: Settings
): OnboardingPreferences{
    override fun isCompleted(): Boolean {
       return setting.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override fun setCompleted(completed: Boolean) {
         setting.getBoolean(KEY_ONBOARDING_COMPLETED, completed)
    }
    private companion object {
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}