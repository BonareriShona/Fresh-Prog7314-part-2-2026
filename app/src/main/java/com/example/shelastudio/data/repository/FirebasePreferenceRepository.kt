package com.example.shelastudio.data.repository

import android.util.Log
import com.example.shelastudio.data.model.UserPreference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePreferenceRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PreferenceRepository {

    companion object {
        private const val TAG = "FirebasePrefRepo"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_PREFS = "preferences"
        private const val DOC_MAIN = "main"
    }

    private fun userId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("No authenticated user")

    private fun prefsDoc(uid: String) = firestore
        .collection(COLLECTION_USERS).document(uid)
        .collection(COLLECTION_PREFS).document(DOC_MAIN)

    override suspend fun getPreferences(): Result<UserPreference> = try {
        val uid = userId()
        val doc = prefsDoc(uid).get().await()
        val prefs = doc.toObject(UserPreference::class.java) ?: UserPreference()
        Result.success(prefs)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to get preferences", e)
        Result.failure(e)
    }

    override suspend fun updatePreferences(prefs: UserPreference): Result<Unit> = try {
        val uid = userId()
        prefsDoc(uid).set(prefs).await()
        Log.i(TAG, "Preferences updated for user $uid")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to update preferences", e)
        Result.failure(e)
    }
}