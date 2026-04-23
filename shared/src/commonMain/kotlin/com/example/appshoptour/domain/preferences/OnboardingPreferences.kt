package com.example.appshoptour.domain.preferences

interface OnboardingPreferences {
    fun isCompleted(): Boolean
    fun setCompleted(completed: Boolean)
}