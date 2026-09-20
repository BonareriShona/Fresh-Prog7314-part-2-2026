package com.example.shelastudio.ui.settings

/**
 * Represents the current state of the Password & Security screen.
 */
data class PasswordSecurityState(
    val email: String = "",
    val signInProvider: String = "",
    val isLoading: Boolean = true,
    val isSendingReset: Boolean = false,
    val resetSuccessful: Boolean = false,
    val error: String? = null
)