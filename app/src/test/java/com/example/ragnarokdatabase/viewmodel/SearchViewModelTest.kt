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
import org.mockito.kotlin.whenever

/**
 * Pruebas unitarias para SearchViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockRepository: ItemRepository

    private lateinit var viewModel: SearchViewModel

    private lateinit var closeable: AutoCloseable

    @Before
    fun setup() {
        closeable = MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun `initial state is Idle`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem() is SearchUiState.Idle)
        }
    }

    @Test
    fun `searchNow with valid query returns Success state`() = runTest {
        // Given
        val items = listOf(
            createSampleItem(501, "Red Potion"),
            createSampleItem(502, "Red Herb")
        )
        whenever(mockRepository.searchItems("red", 50)).thenReturn(items)

        // When
        viewModel.searchNow("red")
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SearchUiState.Success)
            assertEquals(2, (state as SearchUiState.Success).items.size)
        }
    }

    @Test
    fun `searchNow with no results returns Empty state`() = runTest {
        // Given
        whenever(mockRepository.searchItems("nonexistent", 50)).thenReturn(emptyList())

        // When
        viewModel.searchNow("nonexistent")
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            assertTrue(awaitItem() is SearchUiState.Empty)
        }
    }

    @Test
    fun `searchNow with 404 error returns NotFound state`() = runTest {
        // Given
        whenever(mockRepository.searchItems("999999", 50))
            .thenThrow(RuntimeException("404 Not Found"))

        // When
        viewModel.searchNow("999999")
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            assertTrue(awaitItem() is SearchUiState.NotFound)
        }
    }

    @Test
    fun `searchNow with network error returns Error state`() = runTest {
        // Given
        whenever(mockRepository.searchItems("test", 50))
            .thenThrow(RuntimeException("HTTP 500 Server Error"))

        // When
        viewModel.searchNow("test")
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SearchUiState.Error)
            assertEquals("Network error. Please try again.", (state as SearchUiState.Error).message)
        }
    }

    @Test
    fun `searchNow with blank query returns Idle state`() = runTest {
        // When
        viewModel.searchNow("")
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            assertTrue(awaitItem() is SearchUiState.Idle)
        }
    }

    @Test
    fun `onSearchQueryChanged updates searchQuery state`() = runTest {
        // When
        viewModel.onSearchQueryChanged("test query")
        advanceUntilIdle()

        // Then
        viewModel.searchQuery.test {
            assertEquals("test query", awaitItem())
        }
    }

    @Test
    fun `onSearchQueryChanged with debounce performs search after delay`() = runTest {
        // Given
        val items = listOf(createSampleItem(501, "Red Potion"))
        whenever(mockRepository.searchItems("red", 50)).thenReturn(items)

        // When
        viewModel.onSearchQueryChanged("red")

        // Should still be Idle immediately
        viewModel.uiState.test {
            assertTrue(awaitItem() is SearchUiState.Idle)
        }

        // Advance past the debounce delay (500ms)
        advanceTimeBy(501)
        advanceUntilIdle()

        // Then - should have searched
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SearchUiState.Success)
        }
    }

    @Test
    fun `onSearchQueryChanged cancels previous search job`() = runTest {
        // Given
        val items1 = listOf(createSampleItem(501, "Red Potion"))
        val items2 = listOf(createSampleItem(601, "Blue Potion"))
        whenever(mockRepository.searchItems("red", 50)).thenReturn(items1)
        whenever(mockRepository.searchItems("blue", 50)).thenReturn(items2)

        // When - type "red" then quickly change to "blue"
        viewModel.onSearchQueryChanged("red")
        advanceTimeBy(200) // Not enough to trigger search
        viewModel.onSearchQueryChanged("blue")
        advanceTimeBy(501) // Complete debounce for "blue"
        advanceUntilIdle()

        // Then - should only search for "blue"
        viewModel.searchQuery.test {
            assertEquals("blue", awaitItem())
        }
    }

    @Test
    fun `clearSearch resets to Idle state`() = runTest {
        // Given - perform a search first
        val items = listOf(createSampleItem(501, "Red Potion"))
        whenever(mockRepository.searchItems("red", 50)).thenReturn(items)
        viewModel.searchNow("red")
        advanceUntilIdle()

        // When
        viewModel.clearSearch()

        // Then
        viewModel.uiState.test {
            assertTrue(awaitItem() is SearchUiState.Idle)
        }
        viewModel.searchQuery.test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `onSearchQueryChanged with blank query returns Idle`() = runTest {
        // When
        viewModel.onSearchQueryChanged("  ")
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            assertTrue(awaitItem() is SearchUiState.Idle)
        }
    }

    @Test
    fun `searchNow shows Loading state before Success`() = runTest {
        // Given
        val items = listOf(createSampleItem(501, "Red Potion"))
        whenever(mockRepository.searchItems("red", 50)).thenReturn(items)

        // When
        viewModel.uiState.test {
            skipItems(1) // Skip initial Idle state

            viewModel.searchNow("red")

            // Should transition through Loading
            assertTrue(awaitItem() is SearchUiState.Loading)

            advanceUntilIdle()

            // Then reach Success
            assertTrue(awaitItem() is SearchUiState.Success)
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

