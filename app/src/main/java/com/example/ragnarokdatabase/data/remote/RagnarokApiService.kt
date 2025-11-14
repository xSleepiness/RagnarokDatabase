package com.example.ragnarokdatabase.data.remote

import com.example.ragnarokdatabase.model.Item
import com.example.ragnarokdatabase.model.Monster
import com.example.ragnarokdatabase.model.PopularItemsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Declaración de los endpoints que usamos de la API de Ragnarok.
 * Retrofit genera la implementación concreta en base a esta interfaz.
 */
interface RagnarokApiService {

    /**
     * Recupera el detalle de un item por su ID.
     * La API acepta el identificador numérico en el endpoint.
     */
    @GET("items/{id}")
    suspend fun getItem(@Path("id") itemId: Int): Item

    /**
     * Recupera el detalle de un monstruo por su ID.
     * La API acepta el identificador numérico en el endpoint.
     */
    @GET("monsters/{id}")
    suspend fun getMonster(@Path("id") monsterId: Int): Monster

    /**
     * Obtiene los ítems populares según el período de tiempo especificado.
     * period puede ser: today, yesterday, last7days, last30days
     * limit controla cuántos elementos trae la respuesta (por defecto 10)
     */
    @GET("items/popular/{period}")
    suspend fun getPopularItems(
        @Path("period") period: String,
        @Query("limit") limit: Int = 10
    ): PopularItemsResponse
}



