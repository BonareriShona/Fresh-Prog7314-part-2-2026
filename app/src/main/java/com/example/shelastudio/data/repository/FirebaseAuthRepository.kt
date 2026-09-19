package com.example.shelastudio.data.repository

import android.util.Log
import com.example.shelastudio.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication implementation of [AuthRepository].
 *
 * Auth handles credentials; the matching profile document is written to
 * Firestore at users/{uid} so wardrobe, outfits and preferences all hang
 * off the same UID.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    companion object {
        private const val TAG = "FirebaseAuthRepo"
        private const val COLLECTION_USERS = "users"
    }

    override fun currentUserId(): String? = auth.currentUser?.uid

    override fun isSignedIn(): Boolean = auth.currentUser != null

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<String> = try {
        Log.d(TAG, "Email sign-in attempt for $email")
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("Sign-in returned no user")
        Log.i(TAG, "Email sign-in successful. UID=$uid")
        Result.success(uid)
    } catch (e: Exception) {
        Log.e(TAG, "Email sign-in failed", e)
        Result.failure(e)
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        profile: UserProfile
    ): Result<String> = try {
        Log.d(TAG, "Creating account for $email")
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("Account creation returned no user")

        val toSave = profile.copy(
            userId = uid,
            email = email,
            signInProvider = "password"
        )
        firestore.collection(COLLECTION_USERS).document(uid).set(toSave).await()

        Log.i(TAG, "Account created and profile written. UID=$uid")
        Result.success(uid)
    } catch (e: Exception) {
        Log.e(TAG, "Account creation failed", e)
        Result.failure(e)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String> = try {
        Log.d(TAG, "Exchanging Google ID token for Firebase credential")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: error("Google sign-in returned no user")

        // First Google sign-in creates the profile document; later ones leave it alone.
        val docRef = firestore.collection(COLLECTION_USERS).document(user.uid)
        if (!docRef.get().await().exists()) {
            val profile = UserProfile(
                userId = user.uid,
                fullName = user.displayName.orEmpty(),
                email = user.email.orEmpty(),
                photoUrl = user.photoUrl?.toString().orEmpty(),
                signInProvider = "google"
            )
            docRef.set(profile).await()
            Log.i(TAG, "New Google user — profile document created")
        } else {
            Log.d(TAG, "Returning Google user — profile already exists")
        }

        Log.i(TAG, "Google sign-in successful. UID=${user.uid}")
        Result.success(user.uid)
    } catch (e: Exception) {
        Log.e(TAG, "Google sign-in failed", e)
        Result.failure(e)
    }

    override suspend fun getUserProfile(): Result<UserProfile?> = try {
        val uid = currentUserId() ?: error("No authenticated user")
        val doc = firestore.collection(COLLECTION_USERS).document(uid).get().await()
        Result.success(doc.toObject(UserProfile::class.java))
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load user profile", e)
        Result.failure(e)
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = try {
        val uid = currentUserId() ?: error("No authenticated user")
        firestore.collection(COLLECTION_USERS).document(uid)
            .set(profile.copy(userId = uid)).await()
        Log.i(TAG, "Profile updated for UID=$uid")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to update profile", e)
        Result.failure(e)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Log.i(TAG, "Password reset email sent to $email")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Password reset failed", e)
        Result.failure(e)
    }

    override fun signOut() {
        val uid = currentUserId()
        auth.signOut()
        Log.i(TAG, "Signed out. Previous UID=$uid")
    }
}