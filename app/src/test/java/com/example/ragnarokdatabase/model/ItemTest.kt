package com.example.ragnarokdatabase.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para el modelo Item y sus métodos auxiliares
 */
class ItemTest {

    @Test
    fun `getIconUrl returns correct URL format`() {
        val item = createSampleItem(id = 501)
        val expectedUrl = "http://64.176.16.51:8000/api/v1/items/images/item/501.png"

        val actualUrl = item.getIconUrl()

        assertEquals(expectedUrl, actualUrl)
    }

    @Test
    fun `getCollectionImageUrl returns URL with timestamp`() {
        val item = createSampleItem(id = 502)

        val actualUrl = item.getCollectionImageUrl()

        assertTrue(actualUrl.startsWith("http://64.176.16.51:8000/api/v1/items/images/collection/502.png?t="))
        assertTrue(actualUrl.contains("?t="))
    }

    @Test
    fun `getRequiredJobsText returns comma-separated string when jobs exist`() {
        val item = createSampleItem(requiredJob = listOf("Swordsman", "Knight", "Crusader"))

        val result = item.getRequiredJobsText()

        assertEquals("Swordsman, Knight, Crusader", result)
    }

    @Test
    fun `getRequiredJobsText returns null when no jobs required`() {
        val item = createSampleItem(requiredJob = null)

        val result = item.getRequiredJobsText()

        assertNull(result)
    }

    @Test
    fun `getRequiredJobsText returns empty string when jobs list is empty`() {
        val item = createSampleItem(requiredJob = emptyList())

        val result = item.getRequiredJobsText()

        assertEquals("", result)
    }

    @Test
    fun `item with slots greater than zero should display with slots`() {
        val item = createSampleItem(
            name = "Falchion",
            stats = ItemStats(atk = 39, matk = null, defense = null, weight = 60, slots = 3)
        )

        val slots = item.stats.slots
        val displayName = if (slots > 0) "${item.name} [$slots]" else item.name

        assertEquals("Falchion [3]", displayName)
    }

    @Test
    fun `item with zero slots should not display slots`() {
        val item = createSampleItem(
            name = "Sword",
            stats = ItemStats(atk = 25, matk = null, defense = null, weight = 50, slots = 0)
        )

        val slots = item.stats.slots
        val displayName = if (slots > 0) "${item.name} [$slots]" else item.name

        assertEquals("Sword", displayName)
    }

    @Test
    fun `item stats contains correct values`() {
        val stats = ItemStats(
            atk = 150,
            matk = 120,
            defense = 10,
            weight = 100,
            slots = 2
        )

        assertEquals(150, stats.atk)
        assertEquals(120, stats.matk)
        assertEquals(10, stats.defense)
        assertEquals(100, stats.weight)
        assertEquals(2, stats.slots)
    }

    @Test
    fun `item stats with null values`() {
        val stats = ItemStats(
            atk = null,
            matk = null,
            defense = null,
            weight = 50,
            slots = 0
        )

        assertNull(stats.atk)
        assertNull(stats.matk)
        assertNull(stats.defense)
        assertEquals(50, stats.weight)
        assertEquals(0, stats.slots)
    }

    // Helper function to create sample items
    private fun createSampleItem(
        id: Int = 501,
        name: String = "Red Potion",
        description: String = "A healing potion",
        type: String = "Consumable",
        subtype: String? = "Healing",
        buyPrice: Int = 50,
        sellPrice: Int = 25,
        stats: ItemStats = ItemStats(null, null, null, 10, 0),
        requiredLevel: Int = 1,
        requiredJob: List<String>? = null,
        gender: String? = null,
        location: String? = null,
        sprite: String = "red_potion",
        script: String? = null,
        equipScript: String? = null,
        unequipScript: String? = null
    ): Item {
        return Item(
            id = id,
            name = name,
            description = description,
            type = type,
            subtype = subtype,
            buyPrice = buyPrice,
            sellPrice = sellPrice,
            stats = stats,
            requiredLevel = requiredLevel,
            requiredJob = requiredJob,
            gender = gender,
            location = location,
            sprite = sprite,
            script = script,
            equipScript = equipScript,
            unequipScript = unequipScript
        )
    }
}

