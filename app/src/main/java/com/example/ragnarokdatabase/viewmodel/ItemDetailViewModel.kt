package com.example.ragnarokdatabase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ragnarokdatabase.data.repository.ItemRepository
import com.example.ragnarokdatabase.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for the item detail screen.
 * Manages the loading and state of the detailed item information.
 */
class ItemDetailViewModel(
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ItemDetailUiState>(ItemDetailUiState.Loading)
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    private val _uploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val uploadState: StateFlow<ImageUploadState> = _uploadState.asStateFlow()

    fun loadItemDetail(itemId: Int) {
        viewModelScope.launch {
            _uiState.value = ItemDetailUiState.Loading
            try {
                val item = itemRepository.getItem(itemId)
                _uiState.value = ItemDetailUiState.Success(item)
            } catch (e: Exception) {
                _uiState.value = when {
                    e.message?.contains("404") == true -> ItemDetailUiState.NotFound
                    e.message?.contains("HTTP") == true -> ItemDetailUiState.Error("Network error. Please try again.")
                    else -> ItemDetailUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun uploadCollectionImage(itemId: Int, imageFile: File) {
        viewModelScope.launch {
            _uploadState.value = ImageUploadState.Uploading
            try {
                val updatedItem = itemRepository.uploadCollectionImage(itemId, imageFile)

                // Small delay to ensure the backend has saved the image
                kotlinx.coroutines.delay(500)

                _uploadState.value = ImageUploadState.Success

                // Force reload by fetching the item again
                // This ensures we get fresh data and triggers image reload
                try {
                    val refreshedItem = itemRepository.getItem(itemId)
                    _uiState.value = ItemDetailUiState.Success(refreshedItem)
                } catch (e: Exception) {
                    // If refresh fails, still show success with the updated item
                    _uiState.value = ItemDetailUiState.Success(updatedItem)
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("404") == true -> "Item not found"
                    e.message?.contains("HTTP") == true -> "Network error. Please try again."
                    else -> e.message ?: "Unknown error"
                }
                _uploadState.value = ImageUploadState.Error(errorMessage)
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = ImageUploadState.Idle
    }
}

/**
 * Possible states of the detail screen
 */
sealed class ItemDetailUiState {
    object Loading : ItemDetailUiState()
    object NotFound : ItemDetailUiState()
    data class Success(val item: Item) : ItemDetailUiState()
    data class Error(val message: String) : ItemDetailUiState()
}

/**
 * Possible states of image upload
 */
sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Uploading : ImageUploadState()
    object Success : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}

