package com.example.ragnarokdatabase.model

import com.example.ragnarokdatabase.data.remote.NetworkModule
import com.google.gson.annotations.SerializedName

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
    fun getIconUrl(): String = "${NetworkModule.IMAGE_BASE_URL}/items/images/item/$id.png"

    /**
     * Returns the collection image URL with a timestamp to bypass cache
     * This ensures that when an image is updated, the new version is loaded
     */
    fun getCollectionImageUrl(): String {
        val timestamp = System.currentTimeMillis()
        return "${NetworkModule.IMAGE_BASE_URL}/items/images/collection/$id.png?t=$timestamp"
    }

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
    fun getIconUrl(): String = "${NetworkModule.IMAGE_BASE_URL}/items/images/item/$id.png"
}

data class PopularItemsResponse(
    @SerializedName("period")
    val period: String,

    @SerializedName("items")
    val items: List<PopularItem>
)

data class ItemCountResponse(
    @SerializedName("total_items")
    val totalItems: Int
)

