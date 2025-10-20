package com.example.lengua.data

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val role: String? = null // ✅ CAMPO AÑADIDO
)
