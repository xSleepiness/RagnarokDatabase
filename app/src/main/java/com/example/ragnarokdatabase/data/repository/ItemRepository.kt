package com.example.ragnarokdatabase.data.repository

import com.example.ragnarokdatabase.data.remote.NetworkModule
import com.example.ragnarokdatabase.data.remote.RagnarokApiService
import com.example.ragnarokdatabase.model.Item
import com.example.ragnarokdatabase.model.PopularItem
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

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

    /**
     * Searches for items by query (ID or name).
     * If query is a number, searches by ID.
     * If query is text, performs partial search by name.
     */
    suspend fun searchItems(query: String, limit: Int = 50): List<Item> {
        return api.searchItems(query, limit)
    }

    /**
     * Uploads a collection image for an item.
     * @param itemId The ID of the item
     * @param imageFile The PNG file to upload
     * @return Updated Item with new image
     */
    suspend fun uploadCollectionImage(itemId: Int, imageFile: File): Item {
        val requestBody = imageFile.asRequestBody("image/png".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData(
            "file",
            imageFile.name,
            requestBody
        )
        return api.uploadCollectionImage(itemId, multipartBody)
    }
}

