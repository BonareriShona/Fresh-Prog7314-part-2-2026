package com.example.shelastudio.data.repository

import com.example.shelastudio.data.model.ClothingItem

interface WardrobeRepository {

    suspend fun getClothingItems(): Result<List<ClothingItem>>

    suspend fun getClothingItem(itemId: String): Result<ClothingItem?>

    suspend fun addClothingItem(item: ClothingItem): Result<ClothingItem>

    suspend fun updateClothingItem(item: ClothingItem): Result<Unit>

    suspend fun deleteClothingItem(itemId: String): Result<Unit>
}