package com.example.lengua.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Especializacion(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    @SerialName("precio_adicional")
    val precioAdicional: String? = null, // ✅ CAMPO HECHO OPCIONAL
    val activa: Boolean
)

@Serializable
data class EspecializacionesResponse(
    val success: Boolean,
    val data: List<Especializacion>
)
