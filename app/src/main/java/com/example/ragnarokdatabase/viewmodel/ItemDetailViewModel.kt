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
 * ViewModel for the item detail screen.
 * Manages the loading and state of the detailed item information.
 */
class ItemDetailViewModel(
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ItemDetailUiState>(ItemDetailUiState.Loading)
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    fun loadItemDetail(itemId: Int) {
        viewModelScope.launch {
            _uiState.value = ItemDetailUiState.Loading
            try {
                val item = itemRepository.getItem(itemId)
                _uiState.value = ItemDetailUiState.Success(item)
            } catch (e: Exception) {
                _uiState.value = ItemDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

/**
 * Possible states of the detail screen
 */
sealed class ItemDetailUiState {
    object Loading : ItemDetailUiState()
    data class Success(val item: Item) : ItemDetailUiState()
    data class Error(val message: String) : ItemDetailUiState()
}

