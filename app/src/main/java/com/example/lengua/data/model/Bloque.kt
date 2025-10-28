package com.example.lengua.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bloque(
    val id: Int,
    val nombre: String,
    val nivel: String,
    val estado: String,
    @SerialName("grupo_color")
    val grupoColor: String,
    @SerialName("horario_inicio")
    val horarioInicio: String?,
    @SerialName("horario_fin")
    val horarioFin: String?,
    @SerialName("cupo_maximo")
    val cupoMaximo: Int,
    val activo: Boolean,
    @SerialName("estudiantes_count")
    val estudiantesCount: Int
) {
    // Formato para mostrar en el dropdown
    fun getDisplayName(): String {
        return "$nivel $nombre"
    }
}

@Serializable
data class BloquesResponse(
    val success: Boolean,
    val total: Int,
    val bloques: List<Bloque>
)
