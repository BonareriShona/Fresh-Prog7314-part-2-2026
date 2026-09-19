package com.example.shelastudio.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents the authenticated user's profile.
 *
 * Firestore path: users/{userId}
 */
data class UserProfile(

    @DocumentId
    val userId: String = "",

    val fullName: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val dateOfBirth: String = "",
    val photoUrl: String = "",

    /** "google" or "password" — records how the account was created. */
    val signInProvider: String = "",

    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("")
}