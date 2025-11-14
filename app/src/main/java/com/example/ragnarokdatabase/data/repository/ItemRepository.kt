package com.example.ragnarokdatabase.data.repository

import com.example.ragnarokdatabase.data.remote.NetworkModule
import com.example.ragnarokdatabase.data.remote.RagnarokApiService
import com.example.ragnarokdatabase.model.Item
import com.example.ragnarokdatabase.model.PopularItem

/**
 * Encapsula el acceso a la red para que el resto de capas desconozcan
 * si los datos provienen de la web, de un caché o de una base local.
 * Aquí es donde podríamos combinar múltiples fuentes sin cambiar la UI.
 */
class ItemRepository(
    private val api: RagnarokApiService = NetworkModule.api
) {
    /**
     * Consulta el detalle de un item por su ID.
     */
    suspend fun getItem(itemId: Int): Item {
        return api.getItem(itemId)
    }

    /**
     * Obtiene los ítems populares según el período de tiempo.
     * period puede ser: today, yesterday, last7days, last30days
     */
    suspend fun getPopularItems(period: String, limit: Int = 10): List<PopularItem> {
        return api.getPopularItems(period, limit).items
    }
}

