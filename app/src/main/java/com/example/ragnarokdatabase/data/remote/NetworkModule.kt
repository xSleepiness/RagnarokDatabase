package com.example.ragnarokdatabase.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Object that exposes a single Retrofit instance configured to consume the Ragnarok API.
 * This way all collaborators (repositories, tests) reuse the same
 * base configuration without duplicating code.
 */
object NetworkModule {

    // For Android emulator: 10.0.2.2 points to the host's localhost (your PC)
    // For physical device: use your PC's IP on the network (e.g. 192.168.1.42)
    private const val BASE_URL = "http://64.176.16.51:8000/api/v1/"
    
    /**
     * Returns the base URL for API endpoints (includes /api/v1/)
     */
    internal const val API_BASE_URL = BASE_URL
    
    /**
     * Returns the base URL for images (without /api/v1/)
     */
    internal const val IMAGE_BASE_URL = "http://64.176.16.51:8000/api/v1"

    /**
     * Retrofit is created lazily to not initialize the network until it's actually
     * needed. GsonConverterFactory handles mapping JSON ↔ data class.
     */
    val api: RagnarokApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RagnarokApiService::class.java)
    }
}

