package com.example.ragnarokdatabase.data.repository

import com.example.ragnarokdatabase.data.remote.NetworkModule
import com.example.ragnarokdatabase.data.remote.RagnarokApiService
import com.example.ragnarokdatabase.model.Item
import com.example.ragnarokdatabase.model.PopularItem

/**
 * Encapsulates network access so that the rest of the layers don't know
 * whether the data comes from the web, cache, or a local database.
 * This is where we could combine multiple sources without changing the UI.
 */
class ItemRepository(
    private val api: RagnarokApiService = NetworkModule.api
) {
    /**
     * Queries the details of an item by its ID.
     */
    suspend fun getItem(itemId: Int): Item {
        return api.getItem(itemId)
    }

    /**
     * Gets popular items according to the time period.
     * period can be: today, yesterday, last7days, last30days
     */
    suspend fun getPopularItems(period: String, limit: Int = 10): List<PopularItem> {
        return api.getPopularItems(period, limit).items
    }
}

