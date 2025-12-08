package com.example.ragnarokdatabase.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.ragnarokdatabase.data.repository.ItemRepository
import com.example.ragnarokdatabase.model.Item
import com.example.ragnarokdatabase.model.ItemStats
import com.example.ragnarokdatabase.model.ItemType
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
 * Pruebas unitarias para FilterViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FilterViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockRepository: ItemRepository

    private lateinit var viewModel: FilterViewModel

    private lateinit var closeable: AutoCloseable

    @Before
    fun setup() {
        closeable = MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = FilterViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        closeable.close()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem() is FilterUiState.Loading)
        }
    }

    @Test
    fun `loadItemsByType returns Success with items`() = runTest {
        // Given
        val itemTypes = listOf(ItemType(type = "Weapon", count = 100))
        val items = listOf(
            createSampleItem(1201, "Knife"),
            createSampleItem(1202, "Sword")
        )
        whenever(mockRepository.getItemTypes()).thenReturn(itemTypes)
        whenever(mockRepository.filterItemsByType("Weapon", 0, 50)).thenReturn(items)

        // When
        viewModel.loadItemsByType("Weapon", 0)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is FilterUiState.Success)
            assertEquals(2, (state as FilterUiState.Success).items.size)
            assertEquals(2, state.totalPages) // 100 items / 50 per page = 2 pages
        }
    }

    @Test
    fun `loadItemsByType with empty results returns Empty state`() = runTest {
        // Given
        val itemTypes = listOf(ItemType(type = "Weapon", count = 0))
        whenever(mockRepository.getItemTypes()).thenReturn(itemTypes)
        whenever(mockRepository.filterItemsByType("Weapon", 0, 50)).thenReturn(emptyList())

        // When
        viewModel.loadItemsByType("Weapon", 0)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is FilterUiState.Empty)
            assertEquals("No items found for type: Weapon", (state as FilterUiState.Empty).message)
        }
    }

    @Test
    fun `loadItemsByType with 404 error returns Error state`() = runTest {
        // Given
        whenever(mockRepository.getItemTypes()).thenThrow(RuntimeException("404 Not Found"))

        // When
        viewModel.loadItemsByType("InvalidType", 0)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is FilterUiState.Error)
            assertEquals("Items not found", (state as FilterUiState.Error).message)
        }
    }

    @Test
    fun `loadItemsByType with network error returns Error state`() = runTest {
        // Given
        whenever(mockRepository.getItemTypes()).thenThrow(RuntimeException("HTTP 500 Server Error"))

        // When
        viewModel.loadItemsByType("Weapon", 0)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is FilterUiState.Error)
            assertEquals("Network error. Please try again.", (state as FilterUiState.Error).message)
        }
    }

    @Test
    fun `loadItemsByType updates currentType and currentPage`() = runTest {
        // Given
        val itemTypes = listOf(ItemType(type = "Armor", count = 50))
        val items = listOf(createSampleItem(2301, "Cotton Shirt"))
        whenever(mockRepository.getItemTypes()).thenReturn(itemTypes)
        whenever(mockRepository.filterItemsByType("Armor", 50, 50)).thenReturn(items)

        // When
        viewModel.loadItemsByType("Armor", 1)
        advanceUntilIdle()

        // Then
        viewModel.currentType.test {
            assertEquals("Armor", awaitItem())
        }
        viewModel.currentPage.test {
            assertEquals(1, awaitItem())
        }
    }

    @Test
    fun `loadItemsByType calculates correct totalPages`() = runTest {
        // Given - 125 items should result in 3 pages (50 items per page)
        val itemTypes = listOf(ItemType(type = "Consumable", count = 125))
        val items = listOf(createSampleItem(501, "Red Potion"))
        whenever(mockRepository.getItemTypes()).thenReturn(itemTypes)
        whenever(mockRepository.filterItemsByType("Consumable", 0, 50)).thenReturn(items)

        // When
        viewModel.loadItemsByType("Consumable", 0)
        advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is FilterUiState.Success)
            assertEquals(3, (state as FilterUiState.Success).totalPages)
        }
    }

    @Test
    fun `loadNextPage increments page number`() = runTest {
        // Given
        val itemTypes = listOf(ItemType(type = "Weapon", count = 150))
        val items1 = listOf(createSampleItem(1201, "Knife"))
        val items2 = listOf(createSampleItem(1202, "Sword"))
        whenever(mockRepository.getItemTypes()).thenReturn(itemTypes)
        whenever(mockRepository.filterItemsByType("Weapon", 0, 50)).thenReturn(items1)
        whenever(mockRepository.filterItemsByType("Weapon", 50, 50)).thenReturn(items2)

        viewModel.loadItemsByType("Weapon", 0)
        advanceUntilIdle()

        // When
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        viewModel.currentPage.test {
            assertEquals(1, awaitItem())
        }
    }

    @Test
    fun `loadPreviousPage decrements page number`() = runTest {
        // Given
        val itemTypes = listOf(ItemType(type = "Weapon", count = 150))
        val items1 = listOf(createSampleItem(1201, "Knife"))
        val items2 = listOf(createSampleItem(1202, "Sword"))
        whenever(mockRepository.getItemTypes()).thenReturn(itemTypes)
        whenever(mockRepository.filterItemsByType("Weapon", 50, 50)).thenReturn(items2)
        whenever(mockRepository.filterItemsByType("Weapon", 0, 50)).thenReturn(items1)

        viewModel.loadItemsByType("Weapon", 1)
        advanceUntilIdle()

        // When
        viewModel.loadPreviousPage()
        advanceUntilIdle()

        // Then
        viewModel.currentPage.test {
            assertEquals(0, awaitItem())
        }
    }

    @Test
    fun `loadPreviousPage does nothing when on first page`() = runTest {
        // Given
        val itemTypes = listOf(ItemType(type = "Weapon", count = 100))
        val items = listOf(createSampleItem(1201, "Knife"))
        whenever(mockRepository.getItemTypes()).thenReturn(itemTypes)
        whenever(mockRepository.filterItemsByType("Weapon", 0, 50)).thenReturn(items)

        viewModel.loadItemsByType("Weapon", 0)
        advanceUntilIdle()

        // When
        viewModel.loadPreviousPage()
        advanceUntilIdle()

        // Then - should stay on page 0
        viewModel.currentPage.test {
            assertEquals(0, awaitItem())
        }
    }

    @Test
    fun `goToPage loads specific page`() = runTest {
        // Given
        val itemTypes = listOf(ItemType(type = "Weapon", count = 200))
        val items1 = listOf(createSampleItem(1201, "Knife"))
        val items3 = listOf(createSampleItem(1203, "Axe"))
        whenever(mockRepository.getItemTypes()).thenReturn(itemTypes)
        whenever(mockRepository.filterItemsByType("Weapon", 0, 50)).thenReturn(items1)
        whenever(mockRepository.filterItemsByType("Weapon", 100, 50)).thenReturn(items3)

        viewModel.loadItemsByType("Weapon", 0)
        advanceUntilIdle()

        // When
        viewModel.goToPage(2)
        advanceUntilIdle()

        // Then
        viewModel.currentPage.test {
            assertEquals(2, awaitItem())
        }
    }

    @Test
    fun `totalItems updates correctly`() = runTest {
        // Given
        val itemTypes = listOf(ItemType(type = "Card", count = 300))
        val items = listOf(createSampleItem(4001, "Poring Card"))
        whenever(mockRepository.getItemTypes()).thenReturn(itemTypes)
        whenever(mockRepository.filterItemsByType("Card", 0, 50)).thenReturn(items)

        // When
        viewModel.loadItemsByType("Card", 0)
        advanceUntilIdle()

        // Then
        viewModel.totalItems.test {
            assertEquals(300, awaitItem())
        }
    }

    @Test
    fun `loadItemsByType without matching type sets totalItems to 0`() = runTest {
        // Given
        val itemTypes = listOf(ItemType(type = "Weapon", count = 100))
        whenever(mockRepository.getItemTypes()).thenReturn(itemTypes)
        whenever(mockRepository.filterItemsByType("Armor", 0, 50)).thenReturn(emptyList())

        // When
        viewModel.loadItemsByType("Armor", 0)
        advanceUntilIdle()

        // Then
        viewModel.totalItems.test {
            assertEquals(0, awaitItem())
        }
    }

    // Helper function
    private fun createSampleItem(id: Int, name: String): Item {
        return Item(
            id = id,
            name = name,
            description = "Test item",
            type = "Weapon",
            subtype = "Sword",
            buyPrice = 1000,
            sellPrice = 500,
            stats = ItemStats(100, null, null, 100, 0),
            requiredLevel = 1,
            requiredJob = null,
            gender = null,
            location = "Right_Hand",
            sprite = "test",
            script = null,
            equipScript = null,
            unequipScript = null
        )
    }
}

