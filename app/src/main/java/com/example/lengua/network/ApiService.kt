package com.example.lengua.network

import com.example.lengua.data.LoginRequest
import com.example.lengua.data.LoginResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Modelo de datos para la respuesta completa del endpoint de perfil.
 */
@Serializable
data class UserProfileResponse(
    val success: Boolean,
    val user: User
)

/**
 * Modelo que representa al usuario, coincidiendo con la respuesta del backend.
 */
@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name") 
    val lastName: String = "",
    @SerialName("full_name")
    val fullName: String = "",
    val phone: String = "",
    val country: String = "",
    val city: String = "",
    val role: String = "student",
    @SerialName("english_level")
    val englishLevel: String = "",
    
    // ✅ NUEVOS CAMPOS PARA PERFIL COMPLETO:
    @SerialName("birth_date")
    val birthDate: String = "",
    val address: String = "",
    @SerialName("learning_goals")
    val learningGoals: String = "",
    @SerialName("profile_completed")
    val profileCompleted: Boolean = false,
    @SerialName("bloque_asignado")
    val bloqueAsignado: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("correo_personal")
    val correoPersonal: String = ""
)

interface ApiService {
    @POST("login/")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * Obtiene el perfil del usuario autenticado.
     * CORREGIDO: Ahora espera el objeto UserProfileResponse que contiene al usuario.
     */
    @GET("auth/profile/")
    suspend fun getUserProfile(@Header("Authorization") token: String): UserProfileResponse

    // Método para obtener la respuesta cruda (JSON) del backend para depuración
    @GET("auth/profile/")
    suspend fun getUserProfileRaw(@Header("Authorization") token: String): Response<ResponseBody>
}
