package com.example.shelastudio.data.repository

import com.example.shelastudio.data.model.UserPreference

interface PreferenceRepository {
    suspend fun getPreferences(): Result<UserPreference>
    suspend fun updatePreferences(prefs: UserPreference): Result<Unit>
}