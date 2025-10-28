package com.example.lengua.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val username: String? = null,  // Opcional
    val email: String,
    val password: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val role: String,
    @SerialName("bloque_asignado")
    val bloqueAsignado: String? = null,
    val especializacion: String? = null,  // Ahora acepta string
    @SerialName("correo_personal")
    val correoPersonal: String? = null
)
