package com.example.ragnarokdatabase.data.remote

import com.example.ragnarokdatabase.model.Item
import com.example.ragnarokdatabase.model.Monster
import com.example.ragnarokdatabase.model.PopularItemsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Declaration of the endpoints we use from the Ragnarok API.
 * Retrofit generates the concrete implementation based on this interface.
 */
interface RagnarokApiService {

    /**
     * Retrieves the details of an item by its ID.
     * The API accepts the numeric identifier in the endpoint.
     */
    @GET("items/{id}")
    suspend fun getItem(@Path("id") itemId: Int): Item

    /**
     * Retrieves the details of a monster by its ID.
     * The API accepts the numeric identifier in the endpoint.
     */
    @GET("monsters/{id}")
    suspend fun getMonster(@Path("id") monsterId: Int): Monster

    /**
     * Gets popular items according to the specified time period.
     * period can be: today, yesterday, last7days, last30days
     * limit controls how many items the response brings (default 10)
     */
    @GET("items/popular/{period}")
    suspend fun getPopularItems(
        @Path("period") period: String,
        @Query("limit") limit: Int = 10
    ): PopularItemsResponse

    /**
     * Universal search for items.
     * If query is a number → searches by ID
     * If query is text → partial search by name (case-insensitive)
     * limit controls how many items the response brings (default 50, max 500)
     */
    @GET("items/search")
    suspend fun searchItems(
        @Query("query") query: String,
        @Query("limit") limit: Int = 50
    ): List<Item>
}



