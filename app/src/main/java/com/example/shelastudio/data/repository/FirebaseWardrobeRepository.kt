package com.example.shelastudio.data.repository

import android.util.Log
import com.example.shelastudio.data.model.ClothingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseWardrobeRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : WardrobeRepository {

    companion object {
        private const val TAG = "FirebaseWardrobeRepo"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_ITEMS = "clothingItems"
    }

    private fun userId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("No authenticated user")

    private fun wardrobePath(uid: String) =
        firestore.collection(COLLECTION_USERS).document(uid).collection(COLLECTION_ITEMS)

    override suspend fun getClothingItems(): Result<List<ClothingItem>> = try {
        val uid = userId()
        Log.d(TAG, "Fetching wardrobe for user $uid")
        val snapshot = wardrobePath(uid).get().await()
        val items = snapshot.documents.mapNotNull { it.toObject(ClothingItem::class.java) }
        Log.i(TAG, "Loaded ${items.size} clothing items")
        Result.success(items)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to fetch wardrobe", e)
        Result.failure(e)
    }

    override suspend fun getClothingItem(itemId: String): Result<ClothingItem?> = try {
        val uid = userId()
        val doc = wardrobePath(uid).document(itemId).get().await()
        Result.success(doc.toObject(ClothingItem::class.java))
    } catch (e: Exception) {
        Log.e(TAG, "Failed to fetch item $itemId", e)
        Result.failure(e)
    }

    override suspend fun addClothingItem(item: ClothingItem): Result<ClothingItem> = try {
        val uid = userId()
        val docRef = wardrobePath(uid).document()
        val toSave = item.copy(itemId = docRef.id)

        val estKb = (toSave.imageBase64.length + toSave.toString().length) / 1024
        Log.d(TAG, "Saving item with estimated size ~$estKb KB")

        docRef.set(toSave).await()
        Log.i(TAG, "Clothing item ${docRef.id} saved")
        Result.success(toSave)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to add clothing item", e)
        Result.failure(e)
    }

    override suspend fun updateClothingItem(item: ClothingItem): Result<Unit> = try {
        val uid = userId()
        wardrobePath(uid).document(item.itemId).set(item).await()
        Log.i(TAG, "Updated item ${item.itemId}")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to update item", e)
        Result.failure(e)
    }

    override suspend fun deleteClothingItem(itemId: String): Result<Unit> = try {
        val uid = userId()
        wardrobePath(uid).document(itemId).delete().await()
        Log.i(TAG, "Deleted item $itemId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to delete item", e)
        Result.failure(e)
    }
}