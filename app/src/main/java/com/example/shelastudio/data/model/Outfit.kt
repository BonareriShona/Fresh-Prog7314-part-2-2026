package com.example.shelastudio.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a saved outfit composed of one or more clothing items.
 *
 * Firestore mapping:
 *   users/{userId}/outfits/{outfitId}
 *
 * Note: itemIds is a list of ClothingItem.itemId values.
 *       The full items are resolved at read-time by querying clothingItems.
 */
data class Outfit(

    @DocumentId
    val outfitId: String = "",

    val name: String = "",
    val occasion: String = "Casual",
    val season: String = "All-Season",
    val notes: String = "",
    val itemIds: List<String> = emptyList(),

    // Slot map: e.g. {"Top": "itemId", "Bottom": "itemId", "Shoes": "itemId"}
    // Allows the section-based builder UI to know which item is where
    val slots: Map<String, String> = emptyMap(),

    val createdAt: Long = System.currentTimeMillis(),
    val timesWorn: Int = 0,
    val lastWorn: Long = 0L
) {
    constructor() : this("")
}