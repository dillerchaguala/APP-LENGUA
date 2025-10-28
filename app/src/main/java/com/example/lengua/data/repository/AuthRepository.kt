package com.example.lengua.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.lengua.data.LoginRequest
import com.example.lengua.data.model.Bloque
import com.example.lengua.network.ApiService
import com.example.lengua.network.Clase
import com.example.lengua.network.Club
import com.example.lengua.network.Evaluation
import com.example.lengua.network.EvaluationsResponse
import com.example.lengua.network.User
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

// Modelo para el resultado de un login exitoso
data class LoginSuccessData(val token: String, val role: String)

// --- SessionManager CON LOGS DETALLADOS ---
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lengua_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "auth_token"
    }

    fun saveToken(token: String) {
        Log.d("SessionManager", "💾 Guardando token: ${token.take(20)}...")
        prefs.edit().putString(KEY_TOKEN, token).apply()
        val saved = prefs.getString(KEY_TOKEN, null)
        Log.d("SessionManager", "✅ Token guardado y verificado: ${saved?.take(20)}...")
    }

    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        Log.d("SessionManager", "📖 Recuperando token: ${token?.take(20) ?: "NULL"}")
        return token
    }

    fun clearToken() {
        Log.d("SessionManager", "🗑️ Eliminando token")
        prefs.edit().remove(KEY_TOKEN).apply()
    }
}

// --- AuthRepository CON LOGS DETALLADOS ---
class AuthRepository(private val apiService: ApiService, private val sessionManager: SessionManager) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun login(username: String, password: String): Result<LoginSuccessData> {
        return try {
            Log.d("AuthRepo", "🔐 Intentando login con: $username")
            val response = apiService.login(LoginRequest(username, password))
            Log.d("AuthRepo", "✅ Login exitoso para ${username}")
            Log.d("AuthRepo", "🔑 Token recibido: ${response.token.take(20)}...")
            sessionManager.saveToken(response.token)
            Result.Success(LoginSuccessData(token = response.token, role = response.role ?: "student"))
        } catch (e: Exception) {
            Log.e("AuthRepo", "❌ Error en login: ${e.message}", e)
            Result.Error(e.message ?: "Error de login desconocido")
        }
    }

    suspend fun getUserClubs(): Result<List<Club>> {
        return try {
            val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
            val response = apiService.getUserClubs("Bearer $token")
            if (response.success) {
                Result.Success(response.clubs)
            } else {
                Result.Error(response.message ?: "Error al obtener clubs")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun getUserProfile(): Result<User> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
        return try {
            val response = apiService.getUserProfile("Bearer $token")
            if (response.success) Result.Success(response.user) else Result.Error("Error del backend al obtener perfil")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de red al obtener perfil")
        }
    }

    suspend fun updateUserProfile(profileData: Map<String, String>): Result<User> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
        return try {
            val response = apiService.updateUserProfile("Bearer $token", profileData)
            if (response.success) Result.Success(response.user) else Result.Error("Error del backend al actualizar perfil")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de red al actualizar perfil")
        }
    }

    suspend fun getUserClasses(): Result<List<Clase>> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
        return try {
            val response = apiService.getUserClasses("Bearer $token")
            if (response.success) Result.Success(response.clases) else Result.Error("Error del backend al obtener clases")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de red al obtener clases")
        }
    }

    suspend fun getUserEvaluations(): Result<List<Evaluation>> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
        return try {
            val jsonElement = apiService.getUserEvaluations("Bearer $token")
            val response = json.decodeFromJsonElement<EvaluationsResponse>(jsonElement)
            if (response.success) Result.Success(response.evaluaciones) else Result.Error(response.message ?: "Error al obtener evaluaciones")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun getBloques(): Result<List<Bloque>> {
        val token = sessionManager.getToken() ?: return Result.Error("No hay token de autenticación")
        return try {
            val response = apiService.getBloques("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                 if (response.body()!!.success) {
                    Result.Success(response.body()!!.bloques)
                 } else {
                    Result.Error("Error del backend al obtener bloques")
                 }
            } else {
                 Result.Error("Error de red: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de red al obtener bloques")
        }
    }

    fun logout() {
        sessionManager.clearToken()
    }
}
