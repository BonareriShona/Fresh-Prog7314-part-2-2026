package com.example.shelastudio.data.model

/**
 * Weather snapshot used to generate outfit recommendations.
 * Populated from OpenWeather API via our REST API.
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
    /** Converts temperature into a warmth level (1-5) matched against ClothingItem.warmthLevel. */
    fun requiredWarmthLevel(): Int = when {
        temperatureCelsius >= 28 -> 1
        temperatureCelsius >= 20 -> 2
        temperatureCelsius >= 12 -> 3
        temperatureCelsius >= 5  -> 4
        else                     -> 5
    }

    /** Human-readable explanation for the Weather Suggestions screen. */
    fun suitabilityExplanation(): String = when {
        isRainy -> "Water-resistant pieces are ideal for today's rain."
        temperatureCelsius >= 28 -> "Light, breathable layers for the heat."
        temperatureCelsius >= 20 -> "Light layers are ideal for today's mild weather."
        temperatureCelsius >= 12 -> "A light jacket will keep you comfortable."
        temperatureCelsius >= 5  -> "Warm layers for the cooler weather."
        else -> "Bundle up — it's cold outside."
    }

    /** Human-readable temperature with degree symbol. */
    fun formattedTemp(): String = "${temperatureCelsius.toInt()}°C"
}