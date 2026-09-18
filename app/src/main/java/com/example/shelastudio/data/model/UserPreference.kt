package com.example.shelastudio.data.model

/**
 * User preferences stored as a single document per user.
 *
 * Firestore mapping:
 *   users/{userId}/preferences/main
 */
data class UserPreference(
    val language: String = "English",           // English, isiZulu, Afrikaans
    val measurementUnit: String = "cm",         // cm or inches
    val biometricEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val theme: String = "light"
) {
    constructor() : this("English")
}