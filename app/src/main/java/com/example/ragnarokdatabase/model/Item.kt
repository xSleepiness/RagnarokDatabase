package com.example.ragnarokdatabase.model

import com.google.gson.annotations.SerializedName

/**
 * Base URL for image endpoints
 * Note: For Android emulator, 10.0.2.2 points to localhost on the host machine
 * Images are served under /api/v1
 */
private const val BASE_URL = "http://10.0.2.2:8000/api/v1"

data class Item(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("subtype")
    val subtype: String?,

    @SerializedName("buy_price")
    val buyPrice: Int,

    @SerializedName("sell_price")
    val sellPrice: Int,

    @SerializedName("stats")
    val stats: ItemStats,

    @SerializedName("required_level")
    val requiredLevel: Int,

    @SerializedName("required_job")
    val requiredJob: List<String>?,

    @SerializedName("gender")
    val gender: String?,

    @SerializedName("location")
    val location: String?,

    @SerializedName("sprite")
    val sprite: String,

    @SerializedName("script")
    val script: String?,

    @SerializedName("equip_script")
    val equipScript: String?,

    @SerializedName("unequip_script")
    val unequipScript: String?
) {
    fun getIconUrl(): String = "$BASE_URL/items/images/item/$id.png"
    fun getCollectionImageUrl(): String = "$BASE_URL/items/images/collection/$id.png"

    /**
     * Returns the required jobs as a comma-separated string, or null if no jobs required
     */
    fun getRequiredJobsText(): String? {
        return requiredJob?.joinToString(", ")
    }
}

data class ItemStats(
    @SerializedName("atk")
    val atk: Int?,

    @SerializedName("matk")
    val matk: Int?,

    @SerializedName("defense")
    val defense: Int?,

    @SerializedName("weight")
    val weight: Int,

    @SerializedName("slots")
    val slots: Int
)

data class PopularItem(
    @SerializedName("item_id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("view_count")
    val viewCount: Int,

    @SerializedName("sprite")
    val sprite: String? = null
) {
    fun getIconUrl(): String = "$BASE_URL/items/images/item/$id.png"
}

data class PopularItemsResponse(
    @SerializedName("period")
    val period: String,

    @SerializedName("items")
    val items: List<PopularItem>
)

