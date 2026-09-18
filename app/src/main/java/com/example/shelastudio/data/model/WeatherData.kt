package com.example.shelastudio.data.model

/**
 * Weather snapshot used to generate outfit recommendations.
 * Populated from OpenWeather API via our REST API.
 *
 * Not stored in Firestore — fetched live from the weather service.
 */
data class WeatherData(
    val locationName: String = "",
    val temperatureCelsius: Double = 0.0,
    val condition: String = "Clear",
    val description: String = "",
    val isRainy: Boolean = false,
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val fetchedAt: Long = System.currentTimeMillis()
) {
    /**
     * Converts the current temperature into a warmth level (1-5)
     * that can be matched against ClothingItem.warmthLevel.
     *
     * 1 = very light clothing (hot weather)
     * 5 = very warm clothing (cold weather)
     */
    fun requiredWarmthLevel(): Int = when {
        temperatureCelsius >= 28 -> 1
        temperatureCelsius >= 20 -> 2
        temperatureCelsius >= 12 -> 3
        temperatureCelsius >= 5  -> 4
        else                     -> 5
    }
}