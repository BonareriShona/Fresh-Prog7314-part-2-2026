package com.example.shelastudio.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.data.model.Outfit
import com.example.shelastudio.data.model.WeatherData
import com.example.shelastudio.data.repository.OutfitRepository
import com.example.shelastudio.data.repository.WardrobeRepository
import com.example.shelastudio.data.repository.WeatherRepository
import com.example.shelastudio.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val weather: WeatherData? = null,
    val recommendedOutfit: Outfit? = null,
    val recommendedItems: List<ClothingItem> = emptyList(),
    val itemCount: Int = 0,
    val outfitCount: Int = 0,
    val timesWorn: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val wardrobeRepo: WardrobeRepository = RepositoryProvider.wardrobe,
    private val outfitRepo: OutfitRepository = RepositoryProvider.outfits,
    private val weatherRepo: WeatherRepository = RepositoryProvider.weather
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            Log.d(TAG, "Loading home dashboard")
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Weather from REST API (has built-in fallback on failure)
            val weather = weatherRepo.getCurrentWeather("Johannesburg").getOrNull()

            // Wardrobe + outfits from Firebase
            val wardrobe = wardrobeRepo.getClothingItems().getOrDefault(emptyList())
            val outfits = outfitRepo.getOutfits().getOrDefault(emptyList())

            val recommended = outfits.firstOrNull()
            val recommendedItems = recommended?.itemIds
                ?.mapNotNull { id -> wardrobe.firstOrNull { it.itemId == id } }
                ?: emptyList()

            _state.value = HomeState(
                weather = weather,
                recommendedOutfit = recommended,
                recommendedItems = recommendedItems,
                itemCount = wardrobe.size,
                outfitCount = outfits.size,
                timesWorn = wardrobe.sumOf { it.timesWorn },
                isLoading = false,
                error = if (weather == null) "Could not load weather" else null
            )

            Log.i(TAG, "Dashboard loaded: ${wardrobe.size} items, ${outfits.size} outfits")
        }
    }
}