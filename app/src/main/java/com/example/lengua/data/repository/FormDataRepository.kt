package com.example.lengua.data.repository

import com.example.lengua.data.model.Bloque
import com.example.lengua.data.model.Especializacion
import com.example.lengua.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FormDataRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    
    suspend fun getBloques(): Result<List<Bloque>> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.Error("No hay sesión")
            val response = apiService.getBloques("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.Success(body.bloques)
                } else {
                    Result.Error("Error al obtener bloques")
                }
            } else {
                Result.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.localizedMessage}")
        }
    }
    
    suspend fun getEspecializaciones(): Result<List<Especializacion>> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.Error("No hay sesión")
            val response = apiService.getEspecializaciones("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.Success(body.data)
                } else {
                    Result.Error("Error al obtener especializaciones")
                }
            } else {
                Result.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.localizedMessage}")
        }
    }
}
