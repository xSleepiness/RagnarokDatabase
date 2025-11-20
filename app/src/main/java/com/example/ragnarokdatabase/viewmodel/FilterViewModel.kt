package com.example.ragnarokdatabase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ragnarokdatabase.data.repository.ItemRepository
import com.example.ragnarokdatabase.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for filtering items by type with pagination.
 */
class FilterViewModel(
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FilterUiState>(FilterUiState.Loading)
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    private val _currentType = MutableStateFlow<String?>(null)
    val currentType: StateFlow<String?> = _currentType.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalItems = MutableStateFlow(0)
    val totalItems: StateFlow<Int> = _totalItems.asStateFlow()

    private val itemsPerPage = 50

    /**
     * Loads items filtered by type for the specified page.
     */
    fun loadItemsByType(itemType: String, page: Int = 0) {
        viewModelScope.launch {
            _uiState.value = FilterUiState.Loading
            _currentType.value = itemType
            _currentPage.value = page

            try {
                // Get the total count for this type from itemTypes
                val itemTypes = itemRepository.getItemTypes()
                val typeInfo = itemTypes.find { it.type == itemType }
                val total = typeInfo?.count ?: 0
                _totalItems.value = total

                // Get the filtered items
                val skip = page * itemsPerPage
                val items = itemRepository.filterItemsByType(itemType, skip, itemsPerPage)

                if (items.isEmpty()) {
                    _uiState.value = FilterUiState.Empty("No items found for type: $itemType")
                } else {
                    _uiState.value = FilterUiState.Success(
                        items = items,
                        totalPages = (total + itemsPerPage - 1) / itemsPerPage
                    )
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("404") == true -> "Items not found"
                    e.message?.contains("HTTP") == true -> "Network error. Please try again."
                    else -> e.message ?: "Unknown error"
                }
                _uiState.value = FilterUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Loads the next page of items.
     */
    fun loadNextPage() {
        _currentType.value?.let { type ->
            loadItemsByType(type, _currentPage.value + 1)
        }
    }

    /**
     * Loads the previous page of items.
     */
    fun loadPreviousPage() {
        if (_currentPage.value > 0) {
            _currentType.value?.let { type ->
                loadItemsByType(type, _currentPage.value - 1)
            }
        }
    }

    /**
     * Goes to a specific page.
     */
    fun goToPage(page: Int) {
        _currentType.value?.let { type ->
            loadItemsByType(type, page)
        }
    }
}

/**
 * Possible states of the filter screen
 */
sealed class FilterUiState {
    object Loading : FilterUiState()
    data class Success(val items: List<Item>, val totalPages: Int) : FilterUiState()
    data class Empty(val message: String) : FilterUiState()
    data class Error(val message: String) : FilterUiState()
}

