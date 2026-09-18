package com.example.shelastudio.data.repository

import com.example.shelastudio.data.model.ClothingItem

/**
 * Contract for wardrobe data operations.
 *
 * Implementations:
 *   - FirebaseWardrobeRepository (production)
 *   - (Optionally) a fake repository for testing
 */
interface WardrobeRepository {

    /** Fetches all clothing items for the current user. */
    suspend fun getClothingItems(): Result<List<ClothingItem>>

    /** Fetches a single clothing item by ID. */
    suspend fun getClothingItem(itemId: String): Result<ClothingItem?>

    /** Adds a new clothing item to the user's wardrobe. Returns the created item with its ID. */
    suspend fun addClothingItem(item: ClothingItem, imageBytes: ByteArray?): Result<ClothingItem>

    /** Updates an existing clothing item. */
    suspend fun updateClothingItem(item: ClothingItem): Result<Unit>

    /** Deletes a clothing item. */
    suspend fun deleteClothingItem(itemId: String): Result<Unit>
}