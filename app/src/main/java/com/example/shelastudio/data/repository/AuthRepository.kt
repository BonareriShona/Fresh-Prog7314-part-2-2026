package com.example.shelastudio.data.repository

import com.example.shelastudio.data.model.UserProfile

/**
 * Abstraction over the authentication provider so the UI layer
 * never depends on Firebase directly, and so it can be mocked in unit tests.
 */
interface AuthRepository {

    /** Returns the signed-in user's UID, or null if nobody is signed in. */
    fun currentUserId(): String?

    fun isSignedIn(): Boolean

    suspend fun signInWithEmail(email: String, password: String): Result<String>

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        profile: UserProfile
    ): Result<String>

    /** Exchanges a Google ID token for a Firebase session. */
    suspend fun signInWithGoogle(idToken: String): Result<String>

    suspend fun getUserProfile(): Result<UserProfile?>

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>

    suspend fun sendPasswordReset(email: String): Result<Unit>

    fun signOut()
}