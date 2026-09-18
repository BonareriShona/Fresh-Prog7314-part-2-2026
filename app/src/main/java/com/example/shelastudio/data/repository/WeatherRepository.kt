package com.example.shelastudio.data.repository

import com.example.shelastudio.data.model.Outfit

interface OutfitRepository {

    suspend fun getOutfits(): Result<List<Outfit>>
    suspend fun getOutfit(outfitId: String): Result<Outfit?>
    suspend fun createOutfit(outfit: Outfit): Result<Outfit>
    suspend fun updateOutfit(outfit: Outfit): Result<Unit>
    suspend fun deleteOutfit(outfitId: String): Result<Unit>
}