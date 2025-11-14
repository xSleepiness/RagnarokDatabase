package com.example.ragnarokdatabase.model

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
    val requiredJob: String?,

    @SerializedName("gender")
    val gender: String?,

    @SerializedName("location")
    val location: String?,

    @SerializedName("sprite")
    val sprite: String
)

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
    fun getIconUrl(): String = "https://static.divine-pride.net/images/items/item/$id.png"
}

data class PopularItemsResponse(
    @SerializedName("period")
    val period: String,

    @SerializedName("items")
    val items: List<PopularItem>
)

