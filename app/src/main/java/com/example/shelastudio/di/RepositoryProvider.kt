package com.example.shelastudio.di

import com.example.shelastudio.data.repository.FirebaseOutfitRepository
import com.example.shelastudio.data.repository.FirebasePreferenceRepository
import com.example.shelastudio.data.repository.FirebaseWardrobeRepository
import com.example.shelastudio.data.repository.NetworkRecommendationRepository
import com.example.shelastudio.data.repository.NetworkWeatherRepository
import com.example.shelastudio.data.repository.OutfitRepository
import com.example.shelastudio.data.repository.PreferenceRepository
import com.example.shelastudio.data.repository.WardrobeRepository
import com.example.shelastudio.data.repository.WeatherRepository

/**
 * Central repository provider.
 *   - Firebase handles wardrobe, outfits, preferences
 *   - REST API handles weather + recommendations
 */
object RepositoryProvider {

    // Firebase-backed
    val wardrobe: WardrobeRepository by lazy { FirebaseWardrobeRepository() }
    val outfits: OutfitRepository by lazy { FirebaseOutfitRepository() }
    val preferences: PreferenceRepository by lazy { FirebasePreferenceRepository() }

    // REST API-backed
    val weather: WeatherRepository by lazy { NetworkWeatherRepository() }
    val recommendations: NetworkRecommendationRepository by lazy { NetworkRecommendationRepository() }
}