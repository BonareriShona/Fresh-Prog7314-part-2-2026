package com.example.shelastudio.data.repository

import android.util.Log
import com.example.shelastudio.data.model.Outfit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseOutfitRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : OutfitRepository {

    companion object {
        private const val TAG = "FirebaseOutfitRepo"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_OUTFITS = "outfits"
    }

    private fun userId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("No authenticated user")

    private fun outfitsPath(uid: String) =
        firestore.collection(COLLECTION_USERS).document(uid).collection(COLLECTION_OUTFITS)

    override suspend fun getOutfits(): Result<List<Outfit>> = try {
        val uid = userId()
        val snapshot = outfitsPath(uid).get().await()
        val outfits = snapshot.documents.mapNotNull { it.toObject(Outfit::class.java) }
        Log.i(TAG, "Loaded ${outfits.size} outfits for user $uid")
        Result.success(outfits)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to fetch outfits", e)
        Result.failure(e)
    }

    override suspend fun getOutfit(outfitId: String): Result<Outfit?> = try {
        val uid = userId()
        val doc = outfitsPath(uid).document(outfitId).get().await()
        Result.success(doc.toObject(Outfit::class.java))
    } catch (e: Exception) {
        Log.e(TAG, "Failed to fetch outfit $outfitId", e)
        Result.failure(e)
    }

    override suspend fun createOutfit(outfit: Outfit): Result<Outfit> = try {
        val uid = userId()
        val docRef = outfitsPath(uid).document()
        val toSave = outfit.copy(outfitId = docRef.id)
        docRef.set(toSave).await()
        Log.i(TAG, "Created outfit ${docRef.id}")
        Result.success(toSave)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create outfit", e)
        Result.failure(e)
    }

    override suspend fun updateOutfit(outfit: Outfit): Result<Unit> = try {
        val uid = userId()
        outfitsPath(uid).document(outfit.outfitId).set(outfit).await()
        Log.i(TAG, "Updated outfit ${outfit.outfitId}")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to update outfit", e)
        Result.failure(e)
    }

    override suspend fun deleteOutfit(outfitId: String): Result<Unit> = try {
        val uid = userId()
        outfitsPath(uid).document(outfitId).delete().await()
        Log.i(TAG, "Deleted outfit $outfitId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to delete outfit", e)
        Result.failure(e)
    }
}