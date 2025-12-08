package com.example.ragnarokdatabase.data.repository

import com.example.ragnarokdatabase.data.remote.RagnarokApiService
import com.example.ragnarokdatabase.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.io.File

/**
 * Pruebas unitarias para ItemRepository
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItemRepositoryTest {

    @Mock
    private lateinit var mockApi: RagnarokApiService

    @Mock
    private lateinit var mockFile: File

    private lateinit var repository: ItemRepository

    private lateinit var closeable: AutoCloseable

    @Before
    fun setup() {
        closeable = MockitoAnnotations.openMocks(this)
        repository = ItemRepository(mockApi)
    }

    @Test
    fun `getItem returns item from API`() = runTest {
        // Given
        val expectedItem = createSampleItem(501, "Red Potion")
        whenever(mockApi.getItem(501)).thenReturn(expectedItem)

        // When
        val result = repository.getItem(501)

        // Then
        assertEquals(expectedItem, result)
        assertEquals(501, result.id)
        assertEquals("Red Potion", result.name)
    }

    @Test(expected = Exception::class)
    fun `getItem throws exception when API fails`() = runTest {
        // Given
        whenever(mockApi.getItem(999999)).thenThrow(RuntimeException("404 Not Found"))

        // When
        repository.getItem(999999)

        // Then - exception should be thrown
    }

    @Test
    fun `getPopularItems returns list of popular items`() = runTest {
        // Given
        val popularItems = listOf(
            PopularItem(501, "Red Potion", "Consumable", 100),
            PopularItem(502, "Orange Potion", "Consumable", 80)
        )
        val response = PopularItemsResponse(period = "today", items = popularItems)
        whenever(mockApi.getPopularItems("today", 10)).thenReturn(response)

        // When
        val result = repository.getPopularItems("today", 10)

        // Then
        assertEquals(2, result.size)
        assertEquals("Red Potion", result[0].name)
        assertEquals(100, result[0].viewCount)
    }

    @Test
    fun `getPopularItems with different periods`() = runTest {
        // Given
        val yesterdayItems = listOf(PopularItem(503, "Yellow Potion", "Consumable", 60))
        val response = PopularItemsResponse(period = "yesterday", items = yesterdayItems)
        whenever(mockApi.getPopularItems("yesterday", 10)).thenReturn(response)

        // When
        val result = repository.getPopularItems("yesterday", 10)

        // Then
        assertEquals(1, result.size)
        assertEquals("Yellow Potion", result[0].name)
    }

    @Test
    fun `getPopularItems with custom limit`() = runTest {
        // Given
        val items = (1..20).map {
            PopularItem(it, "Item $it", "Consumable", 100 - it)
        }
        val response = PopularItemsResponse(period = "last7days", items = items)
        whenever(mockApi.getPopularItems("last7days", 20)).thenReturn(response)

        // When
        val result = repository.getPopularItems("last7days", 20)

        // Then
        assertEquals(20, result.size)
    }

    @Test
    fun `searchItems returns list of items`() = runTest {
        // Given
        val items = listOf(
            createSampleItem(501, "Red Potion"),
            createSampleItem(507, "Red Herb")
        )
        whenever(mockApi.searchItems("red", 50)).thenReturn(items)

        // When
        val result = repository.searchItems("red", 50)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.name.contains("Red", ignoreCase = true) })
    }

    @Test
    fun `searchItems by ID returns single item`() = runTest {
        // Given
        val items = listOf(createSampleItem(501, "Red Potion"))
        whenever(mockApi.searchItems("501", 50)).thenReturn(items)

        // When
        val result = repository.searchItems("501", 50)

        // Then
        assertEquals(1, result.size)
        assertEquals(501, result[0].id)
    }

    @Test
    fun `searchItems returns empty list when no results`() = runTest {
        // Given
        whenever(mockApi.searchItems("nonexistent", 50)).thenReturn(emptyList())

        // When
        val result = repository.searchItems("nonexistent", 50)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `uploadCollectionImage returns updated item`() = runTest {
        // Given
        val updatedItem = createSampleItem(501, "Red Potion")
        whenever(mockApi.uploadCollectionImage(eq(501), any())).thenReturn(updatedItem)

        // When
        val result = repository.uploadCollectionImage(501, mockFile)

        // Then
        assertEquals(updatedItem, result)
        assertEquals(501, result.id)
    }

    @Test(expected = Exception::class)
    fun `uploadCollectionImage throws exception on API failure`() = runTest {
        // Given
        whenever(mockApi.uploadCollectionImage(eq(999999), any()))
            .thenThrow(RuntimeException("404 Not Found"))

        // When
        repository.uploadCollectionImage(999999, mockFile)

        // Then - exception should be thrown
    }

    @Test
    fun `getItemCount returns total count`() = runTest {
        // Given
        val countResponse = ItemCountResponse(6169)
        whenever(mockApi.getItemCount()).thenReturn(countResponse)

        // When
        val result = repository.getItemCount()

        // Then
        assertEquals(6169, result)
    }

    @Test
    fun `getItemTypes returns list of types with counts`() = runTest {
        // Given
        val types = listOf(
            ItemType("Weapon", 500),
            ItemType("Armor", 300),
            ItemType("Consumable", 200)
        )
        val response = ItemTypesResponse(totalTypes = 3, types = types)
        whenever(mockApi.getItemTypes()).thenReturn(response)

        // When
        val result = repository.getItemTypes()

        // Then
        assertEquals(3, result.size)
        assertEquals("Weapon", result[0].type)
        assertEquals(500, result[0].count)
    }

    @Test
    fun `getItemTypes returns empty list when no types available`() = runTest {
        // Given
        val response = ItemTypesResponse(totalTypes = 0, types = emptyList())
        whenever(mockApi.getItemTypes()).thenReturn(response)

        // When
        val result = repository.getItemTypes()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterItemsByType returns filtered items`() = runTest {
        // Given
        val items = listOf(
            createSampleItem(1201, "Knife"),
            createSampleItem(1202, "Sword")
        )
        whenever(mockApi.filterItemsByType("Weapon", 0, 50)).thenReturn(items)

        // When
        val result = repository.filterItemsByType("Weapon", 0, 50)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.type == "Weapon" })
    }

    @Test
    fun `filterItemsByType with pagination`() = runTest {
        // Given
        val items = listOf(createSampleItem(1251, "Claymore"))
        whenever(mockApi.filterItemsByType("Weapon", 50, 50)).thenReturn(items)

        // When
        val result = repository.filterItemsByType("Weapon", 50, 50)

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `filterItemsByType with custom limit`() = runTest {
        // Given
        val items = (1..20).map { createSampleItem(1200 + it, "Weapon $it") }
        whenever(mockApi.filterItemsByType("Weapon", 0, 20)).thenReturn(items)

        // When
        val result = repository.filterItemsByType("Weapon", 0, 20)

        // Then
        assertEquals(20, result.size)
    }

    @Test
    fun `filterItemsByType returns empty list when no items match`() = runTest {
        // Given
        whenever(mockApi.filterItemsByType("InvalidType", 0, 50)).thenReturn(emptyList())

        // When
        val result = repository.filterItemsByType("InvalidType", 0, 50)

        // Then
        assertTrue(result.isEmpty())
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

