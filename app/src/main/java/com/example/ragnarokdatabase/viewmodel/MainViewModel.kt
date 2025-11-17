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

    init {
        loadPopularItems("today")
    }

    fun loadPopularItems(period: String) {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            _selectedPeriod.value = period
            try {
                val items = itemRepository.getPopularItems(period, limit = 10)
                _uiState.value = MainUiState.Success(items)
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Unknown error")
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
}

/**
 * Possible states of the main screen
 */
sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val items: List<PopularItem>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

