package com.example.shelastudio.data.repository

import android.util.Log
import com.example.shelastudio.data.model.WeatherData
import com.example.shelastudio.data.remote.RetrofitClient

/**
 * REST-backed implementation of WeatherRepository.
 * Calls the ASP.NET Core API which proxies OpenWeather with a secure key.
 */
class NetworkWeatherRepository : WeatherRepository {

    companion object {
        private const val TAG = "NetworkWeatherRepo"
    }

    private val api = RetrofitClient.api

    override suspend fun getCurrentWeather(location: String): Result<WeatherData> = try {
        Log.d(TAG, "Calling REST API: getWeather($location)")
        val dto = api.getWeather(location)
        val weather = WeatherData(
            locationName = dto.locationName,
            temperatureCelsius = dto.temperatureCelsius,
            condition = dto.condition,
            description = dto.description,
            isRainy = dto.isRainy,
            humidity = dto.humidity,
            windSpeed = dto.windSpeed,
            fetchedAt = dto.fetchedAt
        )
        Log.i(TAG, "Weather from API: ${weather.formattedTemp()} ${weather.description}")
        Result.success(weather)
    } catch (e: Exception) {
        Log.e(TAG, "Weather API call failed — using fallback", e)
        // Fallback so the UI never breaks during demos
        Result.success(
            WeatherData(
                locationName = location,
                temperatureCelsius = 22.0,
                condition = "Clouds",
                description = "Partly Cloudy",
                isRainy = false,
                humidity = 55,
                windSpeed = 8.0
            )
        )
    }

    override suspend fun getForecast(location: String, days: Int): Result<List<WeatherData>> = try {
        val current = getCurrentWeather(location).getOrThrow()
        Result.success(listOf(current))
    } catch (e: Exception) {
        Result.failure(e)
    }
}