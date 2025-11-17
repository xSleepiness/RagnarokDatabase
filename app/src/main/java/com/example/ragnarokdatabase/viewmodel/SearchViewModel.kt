package com.example.ragnarokdatabase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ragnarokdatabase.data.repository.ItemRepository
import com.example.ragnarokdatabase.model.Item
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the search screen.
 * Manages the search state and performs item searches with debouncing.
 */
class SearchViewModel(
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: Job? = null

    /**
     * Updates the search query and triggers a search after a debounce delay
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query

        // Cancel previous search job
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        // Debounce search - wait 500ms after user stops typing
        searchJob = viewModelScope.launch {
            delay(500)
            searchItems(query)
        }
    }

    /**
     * Performs immediate search without debouncing
     */
    fun searchNow(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        searchItems(query)
    }

    private fun searchItems(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val results = itemRepository.searchItems(query, limit = 50)
                _uiState.value = if (results.isEmpty()) {
                    SearchUiState.Empty
                } else {
                    SearchUiState.Success(results)
                }
            } catch (e: Exception) {
                _uiState.value = when {
                    e.message?.contains("404") == true -> SearchUiState.NotFound
                    e.message?.contains("HTTP") == true -> SearchUiState.Error("Network error. Please try again.")
                    else -> SearchUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Clears the search and resets to idle state
     */
    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _uiState.value = SearchUiState.Idle
    }
}

/**
 * Possible states of the search screen
 */
sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    object Empty : SearchUiState()
    object NotFound : SearchUiState()
    data class Success(val items: List<Item>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

