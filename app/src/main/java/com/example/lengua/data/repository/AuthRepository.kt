package com.example.lengua.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.lengua.data.LoginRequest
import com.example.lengua.network.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

private const val APP_TAG = "LENGUA_APP"

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

// ✅ NUEVO DATA CLASS
data class LoginSuccessData(val token: String, val role: String)

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("LenguaPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val AUTH_TOKEN = "auth_token"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(AUTH_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(AUTH_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(AUTH_TOKEN).apply()
    }
}

class AuthRepository(private val apiService: ApiService, private val sessionManager: SessionManager) {

    private val json = Json { ignoreUnknownKeys = true }

    // ✅ FUNCIÓN LOGIN MODIFICADA
    suspend fun login(username: String, password: String): Result<LoginSuccessData> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            sessionManager.saveToken(response.token)
            // Ahora devolvemos un objeto con token y rol
            Result.Success(LoginSuccessData(token = response.token, role = response.role ?: "student"))
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de login desconocido")
        }
    }

    suspend fun getUserProfile(): Result<User> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
        return try {
            val response = apiService.getUserProfile("Bearer $token")
            if (response.success) Result.Success(response.user) else Result.Error("Error del backend al obtener perfil")
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error en getUserProfile", e)
            Result.Error(e.message ?: "Error de red al obtener perfil")
        }
    }

    suspend fun updateUserProfile(profileData: Map<String, String>): Result<User> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
        return try {
            val response = apiService.updateUserProfile("Bearer $token", profileData)
            if (response.success) Result.Success(response.user) else Result.Error("Error del backend al actualizar perfil")
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error en updateUserProfile", e)
            Result.Error(e.message ?: "Error de red al actualizar perfil")
        }
    }

    suspend fun getUserClasses(): Result<List<Clase>> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
        return try {
            val response = apiService.getUserClasses("Bearer $token")
            if (response.success) Result.Success(response.clases) else Result.Error("Error del backend al obtener clases")
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error en getUserClasses", e)
            Result.Error(e.message ?: "Error de red al obtener clases")
        }
    }

    suspend fun getUserEvaluations(): Result<List<Evaluation>> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
        return try {
            val jsonElement = apiService.getUserEvaluations("Bearer $token")
            val response = json.decodeFromJsonElement<EvaluationsResponse>(jsonElement)
            if (response.success) {
                Result.Success(response.evaluaciones)
            } else {
                Result.Error(response.message ?: "Error al obtener evaluaciones")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun getBloques(): Result<List<Bloque>> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
        return try {
            val response = apiService.getBloques("Bearer $token")
            if (response.success) Result.Success(response.bloques) else Result.Error("Error del backend al obtener bloques")
        } catch (e: Exception) {
            Log.e(APP_TAG, "Error en getBloques", e)
            Result.Error(e.message ?: "Error de red al obtener bloques")
        }
    }

    fun logout() {
        sessionManager.clearToken()
    }
}
