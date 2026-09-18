package com.example.shelastudio.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a single clothing item in the user's digital wardrobe.
 *
 * Firestore mapping:
 *   users/{userId}/clothingItems/{itemId}
 */
data class ClothingItem(

    @DocumentId
    val itemId: String = "",

    val name: String = "",
    val category: String = "",      // Tops, Bottoms, Dresses, Shoes, Bags, Accessories, Outerwear
    val type: String = "",          // Blazer, T-Shirt, Jeans, etc.
    val colour: String = "",
    val brand: String = "",
    val size: String = "",
    val material: String = "",
    val purchasePrice: Double = 0.0,
    val imageUrl: String = "",
    val season: String = "All-Season",  // Summer, Winter, All-Season
    val occasion: String = "Casual",    // Casual, Work, Evening, Sport
    val warmthLevel: Int = 3,           // 1 = very light, 5 = very warm
    val isWaterResistant: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),

    // Derived fields (not stored separately, but useful for the UI)
    val timesWorn: Int = 0,
    val lastWorn: Long = 0L
) {
    /** Empty constructor required by Firestore */
    constructor() : this("")
}