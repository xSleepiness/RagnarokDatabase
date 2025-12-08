package com.example.ragnarokdatabase.utils

import com.example.ragnarokdatabase.model.Item
import com.example.ragnarokdatabase.model.ItemStats
import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para funciones de utilidad y extensiones
 */
class ItemUtilsTest {

    @Test
    fun `formatItemName with slots appends slot count`() {
        val item = createItem(name = "Falchion", slots = 3)
        val result = formatItemNameWithSlots(item)
        assertEquals("Falchion [3]", result)
    }

    @Test
    fun `formatItemName without slots returns name only`() {
        val item = createItem(name = "Sword", slots = 0)
        val result = formatItemNameWithSlots(item)
        assertEquals("Sword", result)
    }

    @Test
    fun `formatItemName with one slot`() {
        val item = createItem(name = "Stiletto", slots = 1)
        val result = formatItemNameWithSlots(item)
        assertEquals("Stiletto [1]", result)
    }

    @Test
    fun `formatItemName with maximum slots`() {
        val item = createItem(name = "Chain Mail", slots = 4)
        val result = formatItemNameWithSlots(item)
        assertEquals("Chain Mail [4]", result)
    }

    @Test
    fun `hasRequirements returns true when requirements exist`() {
        val item = createItem(
            requiredLevel = 50,
            requiredJob = listOf("Swordsman", "Knight")
        )
        assertTrue(hasRequirements(item))
    }

    @Test
    fun `hasRequirements returns false when level is 0 and no jobs`() {
        val item = createItem(requiredLevel = 0, requiredJob = null)
        assertFalse(hasRequirements(item))
    }

    @Test
    fun `hasRequirements returns false when level is 1 and no jobs`() {
        val item = createItem(requiredLevel = 1, requiredJob = null)
        assertFalse(hasRequirements(item))
    }

    @Test
    fun `hasRequirements returns true when only level requirement exists`() {
        val item = createItem(requiredLevel = 30, requiredJob = null)
        assertTrue(hasRequirements(item))
    }

    @Test
    fun `hasRequirements returns true when only job requirement exists`() {
        val item = createItem(requiredLevel = 1, requiredJob = listOf("Mage"))
        assertTrue(hasRequirements(item))
    }

    @Test
    fun `hasRequirements returns false when job list is empty`() {
        val item = createItem(requiredLevel = 0, requiredJob = emptyList())
        assertFalse(hasRequirements(item))
    }

    @Test
    fun `formatPrice formats buy and sell prices correctly`() {
        assertEquals("1000z", formatPrice(1000))
        assertEquals("50z", formatPrice(50))
        assertEquals("1000000z", formatPrice(1000000))
        assertEquals("0z", formatPrice(0))
    }

    @Test
    fun `formatWeight formats weight correctly`() {
        assertEquals("100", formatWeight(100))
        assertEquals("10", formatWeight(10))
        assertEquals("1", formatWeight(1))
    }

    @Test
    fun `hasStats returns true when item has attack stats`() {
        val item = createItem(atk = 150, matk = null, defense = null)
        assertTrue(hasStats(item))
    }

    @Test
    fun `hasStats returns true when item has matk stats`() {
        val item = createItem(atk = null, matk = 120, defense = null)
        assertTrue(hasStats(item))
    }

    @Test
    fun `hasStats returns true when item has defense stats`() {
        val item = createItem(atk = null, matk = null, defense = 50)
        assertTrue(hasStats(item))
    }

    @Test
    fun `hasStats returns false when item has no combat stats`() {
        val item = createItem(atk = null, matk = null, defense = null)
        assertFalse(hasStats(item))
    }

    @Test
    fun `isEquipment returns true for weapons`() {
        val item = createItem(type = "Weapon", subtype = "Sword")
        assertTrue(isEquipment(item))
    }

    @Test
    fun `isEquipment returns true for armor`() {
        val item = createItem(type = "Armor", subtype = "Headgear")
        assertTrue(isEquipment(item))
    }

    @Test
    fun `isEquipment returns false for consumables`() {
        val item = createItem(type = "Consumable", subtype = "Healing")
        assertFalse(isEquipment(item))
    }

    @Test
    fun `isEquipment returns false for etc items`() {
        val item = createItem(type = "Etc", subtype = null)
        assertFalse(isEquipment(item))
    }

    // Helper functions
    private fun formatItemNameWithSlots(item: Item): String {
        return if (item.stats.slots > 0) {
            "${item.name} [${item.stats.slots}]"
        } else {
            item.name
        }
    }

    private fun hasRequirements(item: Item): Boolean {
        val hasLevelRequirement = item.requiredLevel > 1
        val hasJobRequirement = !item.requiredJob.isNullOrEmpty()
        return hasLevelRequirement || hasJobRequirement
    }

    private fun formatPrice(price: Int): String {
        return "${price}z"
    }

    private fun formatWeight(weight: Int): String {
        return weight.toString()
    }

    private fun hasStats(item: Item): Boolean {
        return item.stats.atk != null || item.stats.matk != null || item.stats.defense != null
    }

    private fun isEquipment(item: Item): Boolean {
        return item.type == "Weapon" || item.type == "Armor"
    }

    private fun createItem(
        id: Int = 1,
        name: String = "Test Item",
        type: String = "Weapon",
        subtype: String? = "Sword",
        requiredLevel: Int = 1,
        requiredJob: List<String>? = null,
        atk: Int? = null,
        matk: Int? = null,
        defense: Int? = null,
        slots: Int = 0
    ): Item {
        return Item(
            id = id,
            name = name,
            description = "Test description",
            type = type,
            subtype = subtype,
            buyPrice = 1000,
            sellPrice = 500,
            stats = ItemStats(atk, matk, defense, 100, slots),
            requiredLevel = requiredLevel,
            requiredJob = requiredJob,
            gender = null,
            location = "Right_Hand",
            sprite = "test",
            script = null,
            equipScript = null,
            unequipScript = null
        )
    }
}

