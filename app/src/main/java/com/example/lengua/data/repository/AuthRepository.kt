package com.example.lengua.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.lengua.data.LoginRequest
import com.example.lengua.network.ApiService
import com.example.lengua.network.User
import com.example.lengua.network.UserProfileResponse

// TAG ÚNICO PARA NUESTRA APP. LO USAREMOS PARA FILTRAR LOGCAT.
private const val APP_TAG = "LENGUA_APP"

/**
 * Una clase sellada para manejar los resultados de las operaciones,
 * ya sea un éxito con datos o un error con un mensaje.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

/**
 * Gestiona el almacenamiento y la recuperación del token de autenticación.
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("LenguaPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val AUTH_TOKEN = "auth_token"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(AUTH_TOKEN, token).apply()
        Log.d(APP_TAG, "[SessionManager] Solicitud para guardar token enviada.")
    }

    fun getToken(): String? {
        return prefs.getString(AUTH_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(AUTH_TOKEN).apply()
        Log.d(APP_TAG, "[SessionManager] Token borrado.")
    }
}

/**
 * Repositorio que maneja la lógica de autenticación y los datos del usuario.
 */
class AuthRepository(private val apiService: ApiService, private val sessionManager: SessionManager) {

    suspend fun login(username: String, password: String): Result<String> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            sessionManager.saveToken(response.token)
            Result.Success(response.token)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de login desconocido")
        }
    }

    suspend fun getUserProfile(): Result<User> {
        return try {
            val token = sessionManager.getToken()
            Log.d(APP_TAG, "[AuthRepository] Intentando obtener perfil con token: $token")

            if (token.isNullOrEmpty()) {
                return Result.Error("No hay token de autenticación guardado")
            }

            val response = apiService.getUserProfile("Bearer $token")
            if (response.success) {
                Result.Success(response.user)
            } else {
                Result.Error("El backend indicó que la obtención del perfil no fue exitosa.")
            }

        } catch (e: Exception) {
            Log.e(APP_TAG, "Error al obtener el perfil", e)
            Result.Error(e.message ?: "Error al obtener el perfil de usuario")
        }
    }

    // NUEVA FUNCIÓN: Borra el token de la sesión.
    fun logout() {
        sessionManager.clearToken()
    }
}
