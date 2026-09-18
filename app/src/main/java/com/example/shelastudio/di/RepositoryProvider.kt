package com.example.shelastudio.di

import com.example.shelastudio.data.repository.FirebaseOutfitRepository
import com.example.shelastudio.data.repository.FirebasePreferenceRepository
import com.example.shelastudio.data.repository.FirebaseWardrobeRepository
import com.example.shelastudio.data.repository.OutfitRepository
import com.example.shelastudio.data.repository.PreferenceRepository
import com.example.shelastudio.data.repository.WardrobeRepository

/**
 * Central place where repository singletons live.
 *
 * Screens and ViewModels access repositories through this object, never
 * instantiating Firebase classes directly. This makes it trivial to swap
 * implementations (e.g., adding a fake repository for tests).
 */
object RepositoryProvider {

    val wardrobe: WardrobeRepository by lazy { FirebaseWardrobeRepository() }
    val outfits: OutfitRepository by lazy { FirebaseOutfitRepository() }
    val preferences: PreferenceRepository by lazy { FirebasePreferenceRepository() }

    // weather: added in Part 8 when REST API is ready
}