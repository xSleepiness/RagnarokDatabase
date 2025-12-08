package com.example.ragnarokdatabase.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.ragnarokdatabase.data.repository.ItemRepository
import com.example.ragnarokdatabase.model.Item
import com.example.ragnarokdatabase.model.ItemStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.io.File

/**
 * Pruebas unitarias para ItemDetailViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItemDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockRepository: ItemRepository

    @Mock
    private lateinit var mockFile: File

    private lateinit var viewModel: ItemDetailViewModel

    private lateinit var closeable: AutoCloseable

    @Before
    fun setup() {
        closeable = MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ItemDetailViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem() is ItemDetailUiState.Loading)
        }
    }

    @Test
    fun `loadItemDetail with valid id returns Success state`() = runTest {
        // Given
        val item = createSampleItem(501, "Red Potion")
        whenever(mockRepository.getItem(501)).thenReturn(item)

        // When
        viewModel.loadItemDetail(501)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ItemDetailUiState.Success)
            assertEquals("Red Potion", (state as ItemDetailUiState.Success).item.name)
            assertEquals(501, state.item.id)
        }
    }

    @Test
    fun `loadItemDetail with 404 error returns NotFound state`() = runTest {
        // Given
        whenever(mockRepository.getItem(999999))
            .thenThrow(RuntimeException("404 Not Found"))

        // When
        viewModel.loadItemDetail(999999)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            assertTrue(awaitItem() is ItemDetailUiState.NotFound)
        }
    }

    @Test
    fun `loadItemDetail with network error returns Error state`() = runTest {
        // Given
        whenever(mockRepository.getItem(501))
            .thenThrow(RuntimeException("HTTP 500 Server Error"))

        // When
        viewModel.loadItemDetail(501)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ItemDetailUiState.Error)
            assertEquals("Network error. Please try again.", (state as ItemDetailUiState.Error).message)
        }
    }

    @Test
    fun `loadItemDetail with generic error returns Error state with message`() = runTest {
        // Given
        whenever(mockRepository.getItem(501))
            .thenThrow(RuntimeException("Database error"))

        // When
        viewModel.loadItemDetail(501)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ItemDetailUiState.Error)
            assertEquals("Database error", (state as ItemDetailUiState.Error).message)
        }
    }

    @Test
    fun `uploadCollectionImage shows Uploading then Success state`() = runTest {
        // Given
        val item = createSampleItem(501, "Red Potion")
        whenever(mockRepository.uploadCollectionImage(501, mockFile)).thenReturn(item)
        whenever(mockRepository.getItem(501)).thenReturn(item)

        // When
        viewModel.uploadState.test {
            assertEquals(ImageUploadState.Idle, awaitItem())

            viewModel.uploadCollectionImage(501, mockFile)

            assertEquals(ImageUploadState.Uploading, awaitItem())

            advanceUntilIdle()

            assertEquals(ImageUploadState.Success, awaitItem())
        }
    }

    @Test
    fun `uploadCollectionImage with 404 error returns Error state`() = runTest {
        // Given
        whenever(mockRepository.uploadCollectionImage(999999, mockFile))
            .thenThrow(RuntimeException("404 Not Found"))

        // When
        viewModel.uploadCollectionImage(999999, mockFile)
        advanceUntilIdle()

        // Then
        viewModel.uploadState.test {
            val state = awaitItem()
            assertTrue(state is ImageUploadState.Error)
            assertEquals("Item not found", (state as ImageUploadState.Error).message)
        }
    }

    @Test
    fun `uploadCollectionImage with network error returns Error state`() = runTest {
        // Given
        whenever(mockRepository.uploadCollectionImage(501, mockFile))
            .thenThrow(RuntimeException("HTTP 500 Server Error"))

        // When
        viewModel.uploadCollectionImage(501, mockFile)
        advanceUntilIdle()

        // Then
        viewModel.uploadState.test {
            val state = awaitItem()
            assertTrue(state is ImageUploadState.Error)
            assertEquals("Network error. Please try again.", (state as ImageUploadState.Error).message)
        }
    }

    @Test
    fun `uploadCollectionImage updates item detail after success`() = runTest {
        // Given
        val originalItem = createSampleItem(501, "Red Potion")
        val updatedItem = createSampleItem(501, "Red Potion Updated")
        whenever(mockRepository.uploadCollectionImage(501, mockFile)).thenReturn(updatedItem)
        whenever(mockRepository.getItem(501)).thenReturn(updatedItem)

        // When
        viewModel.uploadCollectionImage(501, mockFile)
        advanceUntilIdle()

        // Then - item detail should be refreshed
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ItemDetailUiState.Success)
            assertEquals("Red Potion Updated", (state as ItemDetailUiState.Success).item.name)
        }
    }

    @Test
    fun `uploadCollectionImage handles refresh failure gracefully`() = runTest {
        // Given
        val item = createSampleItem(501, "Red Potion")
        whenever(mockRepository.uploadCollectionImage(501, mockFile)).thenReturn(item)
        whenever(mockRepository.getItem(501)).thenThrow(RuntimeException("Refresh failed"))

        // When
        viewModel.uploadCollectionImage(501, mockFile)
        advanceUntilIdle()

        // Then - should still show success with the uploaded item
        viewModel.uploadState.test {
            assertEquals(ImageUploadState.Success, awaitItem())
        }
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ItemDetailUiState.Success)
            assertEquals("Red Potion", (state as ItemDetailUiState.Success).item.name)
        }
    }

    @Test
    fun `resetUploadState resets to Idle`() = runTest {
        // Given - upload something first
        val item = createSampleItem(501, "Red Potion")
        whenever(mockRepository.uploadCollectionImage(501, mockFile)).thenReturn(item)
        whenever(mockRepository.getItem(501)).thenReturn(item)

        viewModel.uploadCollectionImage(501, mockFile)
        advanceUntilIdle()

        // When
        viewModel.resetUploadState()

        // Then
        viewModel.uploadState.test {
            assertEquals(ImageUploadState.Idle, awaitItem())
        }
    }

    @Test
    fun `loadItemDetail shows Loading state before Success`() = runTest {
        // Given
        val item = createSampleItem(501, "Red Potion")
        whenever(mockRepository.getItem(501)).thenReturn(item)

        // When & Then
        viewModel.uiState.test {
            // Initial state is Loading
            assertTrue(awaitItem() is ItemDetailUiState.Loading)

            viewModel.loadItemDetail(501)

            // Should still be Loading or transition to Loading
            val state1 = awaitItem()
            if (state1 is ItemDetailUiState.Loading) {
                // If still Loading, wait for Success
                advanceUntilIdle()
                val state2 = awaitItem()
                assertTrue(state2 is ItemDetailUiState.Success)
            } else {
                // Already Success
                assertTrue(state1 is ItemDetailUiState.Success)
            }
        }
    }

    // Helper function
    private fun createSampleItem(id: Int, name: String): Item {
        return Item(
            id = id,
            name = name,
            description = "Test item",
            type = "Consumable",
            subtype = "Healing",
            buyPrice = 50,
            sellPrice = 25,
            stats = ItemStats(null, null, null, 10, 0),
            requiredLevel = 1,
            requiredJob = null,
            gender = null,
            location = null,
            sprite = "test",
            script = null,
            equipScript = null,
            unequipScript = null
        )
    }
}

