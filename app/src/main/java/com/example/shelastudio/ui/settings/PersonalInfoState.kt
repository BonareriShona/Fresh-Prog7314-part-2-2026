package com.example.shelastudio.ui.settings

import com.example.shelastudio.data.model.UserProfile

/**
 * Represents the current state of the Personal Information screen.
 */
data class PersonalInfoState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccessful: Boolean = false,
    val error: String? = null
)