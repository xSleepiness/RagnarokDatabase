package com.example.ragnarokdatabase.model

import com.google.gson.annotations.SerializedName

data class Monster(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("level")
    val level: Int,

    @SerializedName("element")
    val element: String,

    @SerializedName("element_level")
    val elementLevel: Int,

    @SerializedName("race")
    val race: String,

    @SerializedName("size")
    val size: String,

    @SerializedName("stats")
    val stats: MonsterStats,

    @SerializedName("drops")
    val drops: List<MonsterDrop>,

    @SerializedName("mvp")
    val mvp: Boolean,

    @SerializedName("mvp_drops")
    val mvpDrops: List<MonsterDrop>?,

    @SerializedName("spawn_locations")
    val spawnLocations: List<String>?,

    @SerializedName("sprite")
    val sprite: String
)

data class MonsterStats(
    @SerializedName("hp")
    val hp: Int,

    @SerializedName("sp")
    val sp: Int?,

    @SerializedName("base_exp")
    val baseExp: Int,

    @SerializedName("job_exp")
    val jobExp: Int,

    @SerializedName("atk")
    val atk: Int,

    @SerializedName("atk2")
    val atk2: Int,

    @SerializedName("defense")
    val defense: Int,

    @SerializedName("mdef")
    val mdef: Int,

    @SerializedName("str")
    val str: Int,

    @SerializedName("agi")
    val agi: Int,

    @SerializedName("vit")
    val vit: Int,

    @SerializedName("int")
    val int: Int,

    @SerializedName("dex")
    val dex: Int,

    @SerializedName("luk")
    val luk: Int
)

data class MonsterDrop(
    @SerializedName("item_id")
    val itemId: Int,

    @SerializedName("item_name")
    val itemName: String,

    @SerializedName("rate")
    val rate: Double
)

