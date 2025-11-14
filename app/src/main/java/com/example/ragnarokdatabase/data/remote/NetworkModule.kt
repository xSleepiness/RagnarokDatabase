package com.example.ragnarokdatabase.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto que expone una única instancia de Retrofit configurada para consumir la API de Ragnarok.
 * De esta forma todos los colaboradores (repositorios, pruebas) reutilizan la misma
 * configuración base sin duplicar código.
 */
object NetworkModule {

    // Para emulador Android: 10.0.2.2 apunta al localhost del host (tu PC)
    // Para dispositivo físico: usa la IP de tu PC en la red (ej. 192.168.1.42)
    private const val BASE_URL = "http://10.0.2.2:8000/api/v1/"

    /**
     * Retrofit se crea de forma lazy para no inicializar la red hasta que realmente
     * se necesite. GsonConverterFactory se encarga de mapear JSON ↔ data class.
     */
    val api: RagnarokApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RagnarokApiService::class.java)
    }
}

