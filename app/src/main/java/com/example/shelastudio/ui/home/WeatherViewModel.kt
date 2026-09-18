package com.example.shelastudio.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelastudio.data.model.WeatherData
import com.example.shelastudio.data.repository.NetworkRecommendationRepository
import com.example.shelastudio.data.repository.OutfitRepository
import com.example.shelastudio.data.repository.WardrobeRepository
import com.example.shelastudio.data.repository.WeatherRepository
import com.example.shelastudio.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val weather: WeatherData,
        val suggestions: List<UISuggestion>
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

/**
 * UI-friendly suggestion with the fields we need to render.
 */
data class UISuggestion(
    val name: String,
    val occasion: String,
    val itemIds: List<String>,
    val reason: String,
    val isFromSaved: Boolean
)

class WeatherViewModel(
    private val weatherRepo: WeatherRepository = RepositoryProvider.weather,
    private val wardrobeRepo: WardrobeRepository = RepositoryProvider.wardrobe,
    private val outfitRepo: OutfitRepository = RepositoryProvider.outfits,
    private val recommendRepo: NetworkRecommendationRepository = RepositoryProvider.recommendations
) : ViewModel() {

    companion object {
        private const val TAG = "WeatherViewModel"
    }

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        loadSuggestions()
    }

    fun loadSuggestions() {
        viewModelScope.launch {
            Log.d(TAG, "Loading weather + suggestions")
            _state.value = WeatherUiState.Loading

            val weather = weatherRepo.getCurrentWeather("Johannesburg").getOrNull()
            if (weather == null) {
                _state.value = WeatherUiState.Error("Could not load weather")
                return@launch
            }

            val wardrobe = wardrobeRepo.getClothingItems().getOrDefault(emptyList())
            val outfits = outfitRepo.getOutfits().getOrDefault(emptyList())

            val result = recommendRepo.getSuggestions(weather, wardrobe, outfits)
            if (result.isFailure) {
                Log.e(TAG, "Recommend call failed", result.exceptionOrNull())
                _state.value = WeatherUiState.Success(weather, emptyList())
                return@launch
            }

            val suggestions = result.getOrDefault(emptyList()).map {
                UISuggestion(
                    name = it.name,
                    occasion = it.occasion,
                    itemIds = it.itemIds,
                    reason = it.reason,
                    isFromSaved = it.isFromSaved
                )
            }

            Log.i(TAG, "Got ${suggestions.size} suggestions")
            _state.value = WeatherUiState.Success(weather, suggestions)
        }
    }
}