package com.example.ragnarokdatabase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ragnarokdatabase.data.repository.ItemRepository
import com.example.ragnarokdatabase.model.PopularItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the main screen.
 * Manages the state of popular items according to different time periods.
 */
class MainViewModel(
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("today")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _totalItemsCount = MutableStateFlow<Int?>(null)
    val totalItemsCount: StateFlow<Int?> = _totalItemsCount.asStateFlow()

    init {
        loadPopularItems("today")
        loadTotalItemsCount()
    }

    fun loadPopularItems(period: String) {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            _selectedPeriod.value = period
            try {
                val items = itemRepository.getPopularItems(period, limit = 10)
                _uiState.value = MainUiState.Success(items)
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("404") == true -> "Popular items not found"
                    e.message?.contains("HTTP") == true -> "Network error. Please try again."
                    else -> e.message ?: "Unknown error"
                }
                _uiState.value = MainUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Reloads popular items with the currently selected period.
     * Useful for refreshing the list after navigating back.
     */
    fun refreshCurrentPeriod() {
        loadPopularItems(_selectedPeriod.value)
    }

    /**
     * Loads the total count of items in the database.
     */
    private fun loadTotalItemsCount() {
        viewModelScope.launch {
            try {
                val count = itemRepository.getItemCount()
                _totalItemsCount.value = count
            } catch (e: Exception) {
                // If there's an error, we just keep the count as null
                _totalItemsCount.value = null
            }
        }
    }
}

/**
 * Possible states of the main screen
 */
sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val items: List<PopularItem>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

