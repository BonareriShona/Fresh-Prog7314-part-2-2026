package com.example.shelastudio.data.repository

import com.example.shelastudio.data.model.WeatherData

interface WeatherRepository {

    /** Fetches current weather for a given city (or user's location). */
    suspend fun getCurrentWeather(location: String): Result<WeatherData>

    /** Fetches forecast for the next N days. */
    suspend fun getForecast(location: String, days: Int = 3): Result<List<WeatherData>>
}