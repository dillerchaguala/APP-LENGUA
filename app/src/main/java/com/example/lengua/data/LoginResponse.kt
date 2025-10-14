package com.example.lengua.data

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String
)