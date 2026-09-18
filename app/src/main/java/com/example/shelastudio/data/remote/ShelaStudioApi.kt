package com.example.shelastudio.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for the ShelaStudio REST API.
 */
interface ShelaStudioApi {

    @GET("api/weather")
    suspend fun getWeather(@Query("city") city: String): WeatherDto

    @POST("api/recommend")
    suspend fun getRecommendations(
        @Body request: RecommendRequestDto
    ): RecommendResponseDto
}