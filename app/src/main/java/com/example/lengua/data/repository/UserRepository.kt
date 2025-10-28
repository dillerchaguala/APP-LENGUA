package com.example.lengua.data.repository

import com.example.lengua.data.model.CreateUserRequest
import com.example.lengua.data.model.User
import com.example.lengua.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.Error("No hay sesión")
            val response = apiService.getUsers("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Error al obtener usuarios: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.localizedMessage}")
        }
    }
    
    suspend fun createUser(request: CreateUserRequest): Result<User> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.Error("No hay sesión")
            val response = apiService.createUser("Bearer $token", request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.user != null) {
                    Result.Success(body.user)
                } else {
                    val errorMsg = body.errors?.entries?.joinToString("\n") { (field, errors) ->
                        "$field: ${errors.joinToString(", ")}"
                    } ?: "Error al crear usuario"
                    Result.Error(errorMsg)
                }
            } else {
                Result.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.localizedMessage}")
        }
    }
}
