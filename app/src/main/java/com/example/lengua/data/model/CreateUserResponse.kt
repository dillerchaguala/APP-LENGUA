package com.example.lengua.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserResponse(
    val success: Boolean,
    val user: User? = null,
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
)
