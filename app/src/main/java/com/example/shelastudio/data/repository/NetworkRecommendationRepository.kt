package com.example.shelastudio.data.repository

import android.util.Log
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.data.model.Outfit
import com.example.shelastudio.data.model.WeatherData
import com.example.shelastudio.data.remote.ClothingItemInputDto
import com.example.shelastudio.data.remote.OutfitInputDto
import com.example.shelastudio.data.remote.RecommendRequestDto
import com.example.shelastudio.data.remote.RetrofitClient
import com.example.shelastudio.data.remote.WeatherInputDto

/**
 * Suggestion shape returned to the UI after mapping from the API DTO.
 */
data class ApiSuggestion(
    val name: String,
    val occasion: String,
    val itemIds: List<String>,
    val reason: String,
    val isFromSaved: Boolean
)

/**
 * REST-backed recommendation repository.
 */
class NetworkRecommendationRepository {

    companion object {
        private const val TAG = "NetworkRecRepo"
    }

    private val api = RetrofitClient.api

    suspend fun getSuggestions(
        weather: WeatherData,
        wardrobe: List<ClothingItem>,
        savedOutfits: List<Outfit>
    ): Result<List<ApiSuggestion>> = try {
        Log.d(TAG, "Calling REST API: recommend with ${wardrobe.size} items")

        val request = RecommendRequestDto(
            weather = WeatherInputDto(
                locationName = weather.locationName,
                temperatureCelsius = weather.temperatureCelsius,
                condition = weather.condition,
                description = weather.description,
                isRainy = weather.isRainy
            ),
            wardrobe = wardrobe.map {
                ClothingItemInputDto(
                    itemId = it.itemId,
                    name = it.name,
                    category = it.category,
                    season = it.season,
                    warmthLevel = it.warmthLevel,
                    isWaterResistant = it.isWaterResistant
                )
            },
            savedOutfits = savedOutfits.map {
                OutfitInputDto(
                    outfitId = it.outfitId,
                    name = it.name,
                    occasion = it.occasion,
                    itemIds = it.itemIds
                )
            }
        )

        val response = api.getRecommendations(request)
        val suggestions = response.suggestions.map {
            ApiSuggestion(
                name = it.name,
                occasion = it.occasion,
                itemIds = it.itemIds,
                reason = it.reason,
                isFromSaved = it.isFromSaved
            )
        }
        Log.i(TAG, "Received ${suggestions.size} suggestions from API")
        Result.success(suggestions)
    } catch (e: Exception) {
        Log.e(TAG, "Recommendation API call failed", e)
        Result.failure(e)
    }
}