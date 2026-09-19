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
import com.example.shelastudio.data.repository.AuthRepository
import com.example.shelastudio.data.repository.FirebaseAuthRepository

/**
 * Central repository provider.
 *   - Firebase handles wardrobe, outfits, preferences
 *   - REST API handles weather + recommendations
 */
object RepositoryProvider {

    // Authentication
    val auth: AuthRepository by lazy { FirebaseAuthRepository() }
    // Firebase-backed
    val wardrobe: WardrobeRepository by lazy { FirebaseWardrobeRepository() }
    val outfits: OutfitRepository by lazy { FirebaseOutfitRepository() }
    val preferences: PreferenceRepository by lazy { FirebasePreferenceRepository() }

    // REST API-backed
    val weather: WeatherRepository by lazy { NetworkWeatherRepository() }
    val recommendations: NetworkRecommendationRepository by lazy { NetworkRecommendationRepository() }
}