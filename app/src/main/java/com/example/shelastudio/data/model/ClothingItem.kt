package com.example.shelastudio.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a single clothing item in the user's digital wardrobe.
 *
 * Firestore path: users/{userId}/clothingItems/{itemId}
 *
 * The image is stored directly as a Base64 string (no Cloud Storage needed).
 */
data class ClothingItem(

    @DocumentId
    val itemId: String = "",

    val name: String = "",
    val category: String = "",
    val type: String = "",
    val colour: String = "",
    val brand: String = "",
    val size: String = "",
    val material: String = "",
    val purchasePrice: Double = 0.0,

    /** Base64-encoded JPEG. Firestore limit is 1 MB per document. */
    val imageBase64: String = "",

    val season: String = "All-Season",
    val occasion: String = "Casual",
    val warmthLevel: Int = 3,
    val isWaterResistant: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),

    val timesWorn: Int = 0,
    val lastWorn: Long = 0L
) {
    constructor() : this("")
}