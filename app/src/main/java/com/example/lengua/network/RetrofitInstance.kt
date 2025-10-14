package com.example.lengua.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8000/api/"

    // Se necesita una instancia de Json para el conversor
    private val json = Json {
        ignoreUnknownKeys = true // Ignora campos en el JSON que no estén en la data class
        coerceInputValues = true // Convierte los 'null' del JSON a valores por defecto (ej: "", 0, false)
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory(MediaType.parse("application/json")!!))
            .build()
            .create(ApiService::class.java)
    }
}
