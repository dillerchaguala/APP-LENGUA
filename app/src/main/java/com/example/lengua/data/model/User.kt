package com.example.lengua.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val role: String,
    @SerialName("bloque_asignado")
    val bloqueAsignado: String? = null,
    val especializacion: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)
