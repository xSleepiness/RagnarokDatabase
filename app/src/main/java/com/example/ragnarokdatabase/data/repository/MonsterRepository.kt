package com.example.ragnarokdatabase.data.repository

import com.example.ragnarokdatabase.data.remote.NetworkModule
import com.example.ragnarokdatabase.data.remote.RagnarokApiService
import com.example.ragnarokdatabase.model.Monster

/**
 * Encapsula el acceso a la red para que el resto de capas desconozcan
 * si los datos provienen de la web, de un caché o de una base local.
 * Aquí es donde podríamos combinar múltiples fuentes sin cambiar la UI.
 */
class MonsterRepository(
    private val api: RagnarokApiService = NetworkModule.api
) {
    /**
     * Consulta el detalle de un monstruo por su ID.
     */
    suspend fun getMonster(monsterId: Int): Monster {
        return api.getMonster(monsterId)
    }
}

