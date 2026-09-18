package com.example.shelastudio.ui.outfit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.data.model.Outfit
import com.example.shelastudio.data.repository.OutfitRepository
import com.example.shelastudio.data.repository.WardrobeRepository
import com.example.shelastudio.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for outfit screens.
 */
sealed class OutfitUiState {
    object Loading : OutfitUiState()
    data class Success(val outfits: List<Outfit>) : OutfitUiState()
    data class Error(val message: String) : OutfitUiState()
}

/**
 * UI state for the create outfit screen (wardrobe items + draft outfit).
 */
data class OutfitBuilderState(
    val wardrobe: List<ClothingItem> = emptyList(),
    val selectedSlots: Map<String, ClothingItem> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class OutfitViewModel(
    private val outfitRepo: OutfitRepository = RepositoryProvider.outfits,
    private val wardrobeRepo: WardrobeRepository = RepositoryProvider.wardrobe
) : ViewModel() {

    companion object {
        private const val TAG = "OutfitViewModel"

        /** The predefined slots for section-based outfit building. */
        val SLOTS = listOf("Top", "Bottom", "Outerwear", "Shoes", "Accessory")
    }

    // ---- Outfit list state ----
    private val _uiState = MutableStateFlow<OutfitUiState>(OutfitUiState.Loading)
    val uiState: StateFlow<OutfitUiState> = _uiState.asStateFlow()

    // ---- Outfit builder state ----
    private val _builderState = MutableStateFlow(OutfitBuilderState())
    val builderState: StateFlow<OutfitBuilderState> = _builderState.asStateFlow()

    init {
        loadOutfits()
    }

    // ============================================================
    //  OUTFIT LIST
    // ============================================================

    fun loadOutfits() {
        viewModelScope.launch {
            Log.d(TAG, "Loading outfits")
            _uiState.value = OutfitUiState.Loading
            outfitRepo.getOutfits()
                .onSuccess { outfits ->
                    Log.i(TAG, "Loaded ${outfits.size} outfits")
                    _uiState.value = OutfitUiState.Success(outfits)
                }
                .onFailure { e ->
                    Log.e(TAG, "Outfit load failed", e)
                    _uiState.value = OutfitUiState.Error(
                        e.message ?: "Could not load outfits"
                    )
                }
        }
    }

    fun deleteOutfit(outfitId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Deleting outfit $outfitId")
            outfitRepo.deleteOutfit(outfitId)
                .onSuccess {
                    Log.i(TAG, "Deleted $outfitId")
                    loadOutfits()
                }
                .onFailure { e -> Log.e(TAG, "Delete failed", e) }
        }
    }

    // ============================================================
    //  OUTFIT BUILDER
    // ============================================================

    /**
     * Loads the wardrobe items so the user can select them for the outfit.
     */
    fun loadWardrobeForBuilder() {
        viewModelScope.launch {
            _builderState.value = _builderState.value.copy(isLoading = true, error = null)
            wardrobeRepo.getClothingItems()
                .onSuccess { items ->
                    Log.i(TAG, "Builder wardrobe loaded: ${items.size} items")
                    _builderState.value = _builderState.value.copy(
                        wardrobe = items,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "Builder wardrobe load failed", e)
                    _builderState.value = _builderState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Could not load wardrobe"
                    )
                }
        }
    }

    /**
     * Assigns an item to a slot (Top, Bottom, etc.).
     * Passing null clears the slot.
     */
    fun setSlot(slot: String, item: ClothingItem?) {
        val current = _builderState.value.selectedSlots.toMutableMap()
        if (item == null) {
            current.remove(slot)
        } else {
            current[slot] = item
        }
        Log.d(TAG, "Slot '$slot' set to ${item?.name ?: "(empty)"}")
        _builderState.value = _builderState.value.copy(selectedSlots = current)
    }

    fun clearBuilder() {
        _builderState.value = OutfitBuilderState()
    }

    /**
     * Saves the current draft outfit. Returns the outfitId on success.
     */
    fun saveOutfit(
        name: String,
        occasion: String,
        season: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val slots = _builderState.value.selectedSlots
            if (slots.isEmpty()) {
                Log.w(TAG, "Cannot save outfit with no items")
                onComplete(false)
                return@launch
            }

            val itemIds = slots.values.map { it.itemId }
            val slotMap = slots.mapValues { it.value.itemId }

            val outfit = Outfit(
                name = name,
                occasion = occasion,
                season = season,
                itemIds = itemIds,
                slots = slotMap
            )

            Log.d(TAG, "Saving outfit: $outfit")
            outfitRepo.createOutfit(outfit)
                .onSuccess {
                    Log.i(TAG, "Outfit saved successfully")
                    onComplete(true)
                }
                .onFailure { e ->
                    Log.e(TAG, "Outfit save failed", e)
                    onComplete(false)
                }
        }
    }
}