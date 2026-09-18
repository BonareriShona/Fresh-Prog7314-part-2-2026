package com.example.shelastudio.data.remote

import com.google.gson.annotations.SerializedName

/**
 * DTOs for the ShelaStudio REST API.
 * Field names must match the ASP.NET Core JSON output exactly.
 */

// ---------- WEATHER ----------
data class WeatherDto(
    @SerializedName("locationName") val locationName: String,
    @SerializedName("temperatureCelsius") val temperatureCelsius: Double,
    @SerializedName("condition") val condition: String,
    @SerializedName("description") val description: String,
    @SerializedName("isRainy") val isRainy: Boolean,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("windSpeed") val windSpeed: Double,
    @SerializedName("fetchedAt") val fetchedAt: Long
)

// ---------- RECOMMEND REQUEST ----------
data class RecommendRequestDto(
    @SerializedName("weather") val weather: WeatherInputDto,
    @SerializedName("wardrobe") val wardrobe: List<ClothingItemInputDto>,
    @SerializedName("savedOutfits") val savedOutfits: List<OutfitInputDto>
)

data class WeatherInputDto(
    @SerializedName("locationName") val locationName: String,
    @SerializedName("temperatureCelsius") val temperatureCelsius: Double,
    @SerializedName("condition") val condition: String,
    @SerializedName("description") val description: String,
    @SerializedName("isRainy") val isRainy: Boolean
)

data class ClothingItemInputDto(
    @SerializedName("itemId") val itemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("season") val season: String,
    @SerializedName("warmthLevel") val warmthLevel: Int,
    @SerializedName("isWaterResistant") val isWaterResistant: Boolean
)

data class OutfitInputDto(
    @SerializedName("outfitId") val outfitId: String,
    @SerializedName("name") val name: String,
    @SerializedName("occasion") val occasion: String,
    @SerializedName("itemIds") val itemIds: List<String>
)

// ---------- RECOMMEND RESPONSE ----------
data class RecommendResponseDto(
    @SerializedName("suggestions") val suggestions: List<SuggestionDto>
)

data class SuggestionDto(
    @SerializedName("name") val name: String,
    @SerializedName("occasion") val occasion: String,
    @SerializedName("itemIds") val itemIds: List<String>,
    @SerializedName("reason") val reason: String,
    @SerializedName("isFromSaved") val isFromSaved: Boolean
)