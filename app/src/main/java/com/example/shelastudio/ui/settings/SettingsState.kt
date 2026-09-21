package com.example.shelastudio.ui.settings

import com.example.shelastudio.data.model.UserPreference

data class SettingsState(
    val preferences: UserPreference = UserPreference(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccessful: Boolean = false,
    val error: String? = null
)