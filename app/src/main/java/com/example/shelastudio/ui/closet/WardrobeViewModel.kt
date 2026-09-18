package com.example.shelastudio.ui.closet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shelastudio.data.model.ClothingItem
import com.example.shelastudio.data.repository.WardrobeRepository
import com.example.shelastudio.di.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WardrobeUiState {
    object Loading : WardrobeUiState()
    data class Success(val items: List<ClothingItem>) : WardrobeUiState()
    data class Error(val message: String) : WardrobeUiState()
}

class WardrobeViewModel(
    private val repository: WardrobeRepository = RepositoryProvider.wardrobe
) : ViewModel() {

    companion object {
        private const val TAG = "WardrobeViewModel"
    }

    private val _uiState = MutableStateFlow<WardrobeUiState>(WardrobeUiState.Loading)
    val uiState: StateFlow<WardrobeUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private var allItems: List<ClothingItem> = emptyList()

    init {
        loadWardrobe()
    }

    fun loadWardrobe() {
        viewModelScope.launch {
            Log.d(TAG, "Loading wardrobe")
            _uiState.value = WardrobeUiState.Loading
            repository.getClothingItems()
                .onSuccess { items ->
                    allItems = items
                    Log.i(TAG, "Loaded ${items.size} items")
                    applyFilter()
                }
                .onFailure { e ->
                    Log.e(TAG, "Load failed", e)
                    _uiState.value = WardrobeUiState.Error(
                        e.message ?: "Could not load wardrobe"
                    )
                }
        }
    }

    fun setCategory(category: String) {
        Log.d(TAG, "Filter category set to $category")
        _selectedCategory.value = category
        applyFilter()
    }

    private fun applyFilter() {
        val category = _selectedCategory.value
        val filtered = if (category == "All") allItems
        else allItems.filter { it.category == category }
        _uiState.value = WardrobeUiState.Success(filtered)
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Deleting item $itemId")
            repository.deleteClothingItem(itemId)
                .onSuccess {
                    Log.i(TAG, "Deleted $itemId")
                    loadWardrobe()
                }
                .onFailure { e ->
                    Log.e(TAG, "Delete failed", e)
                }
        }
    }

    fun saveItem(item: ClothingItem) {
        viewModelScope.launch {
            Log.d(TAG, "Saving item ${item.name}")
            repository.addClothingItem(item)
                .onSuccess {
                    Log.i(TAG, "Item saved successfully")
                }
                .onFailure { e ->
                    Log.e(TAG, "Save failed", e)
                }
        }
    }
}