package com.example.ragnarokdatabase.data.repository

import com.example.ragnarokdatabase.data.remote.NetworkModule
import com.example.ragnarokdatabase.data.remote.RagnarokApiService
import com.example.ragnarokdatabase.model.Monster

/**
 * Encapsulates network access so that the rest of the layers don't know
 * whether the data comes from the web, cache, or a local database.
 * This is where we could combine multiple sources without changing the UI.
 */
class MonsterRepository(
    private val api: RagnarokApiService = NetworkModule.api
) {
    /**
     * Queries the details of a monster by its ID.
     */
    suspend fun getMonster(monsterId: Int): Monster {
        return api.getMonster(monsterId)
    }
}

