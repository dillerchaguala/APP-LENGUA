package com.example.lengua.network

import com.example.lengua.data.LoginRequest
import com.example.lengua.data.LoginResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

// --- Modelos de Datos para el Perfil ---

@Serializable
data class UserProfileResponse(
    val success: Boolean,
    val user: User
)

@Serializable
data class UpdateProfileResponse(
    val success: Boolean,
    val user: User
)

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    @SerialName("full_name") val fullName: String = "",
    val phone: String = "",
    val country: String = "",
    val city: String = "",
    val role: String = "student",
    @SerialName("english_level") val englishLevel: String = "",
    @SerialName("birth_date") val birthDate: String = "",
    val address: String = "",
    @SerialName("learning_goals") val learningGoals: String = "",
    @SerialName("profile_completed") val profileCompleted: Boolean = false,
    @SerialName("bloque_asignado") val bloqueAsignado: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("correo_personal") val correoPersonal: String = ""
)

// --- Modelos de Datos para las Clases ---

@Serializable
data class ClassesResponse(
    val success: Boolean,
    val total: Int,
    val clases: List<Clase>
)

@Serializable
data class Clase(
    val id: Int,
    val nombre: String,
    val profesor: String,
    val fecha: String,
    val hora: String,
    val duracion: Int,
    val tema: String,
    val descripcion: String? = null,
    @SerialName("tipo_clase") val tipoClase: String,
    val modalidad: String,
    @SerialName("meet_link") val meetLink: String? = null,
    val estado: String,
    @SerialName("created_at") val createdAt: String
)

// --- Modelos de Datos para las Evaluaciones ---

@Serializable
data class EvaluationsResponse(
    val success: Boolean,
    val total: Int,
    val evaluaciones: List<Evaluation>
)

@Serializable
data class Evaluation(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val tipo: String,  // "quiz", "examen", "tarea"
    val profesor: String,
    @SerialName("fecha_limite") val fechaLimite: String,
    @SerialName("archivo_url") val archivoUrl: String?,
    @SerialName("estado_estudiante") val estadoEstudiante: String,  // "pendiente", "entregada"
    @SerialName("fecha_entrega") val fechaEntrega: String?,
    val calificacion: Float?,  // Puede ser null
    @SerialName("created_at") val createdAt: String
)


// --- Interfaz del Servicio API ---

interface ApiService {
    @POST("login/")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("auth/profile/")
    suspend fun getUserProfile(@Header("Authorization") token: String): UserProfileResponse

    @PUT("profile/update/")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body profileData: Map<String, String>
    ): UpdateProfileResponse

    @GET("classes/")
    suspend fun getUserClasses(@Header("Authorization") token: String): ClassesResponse

    // ✅ ENDPOINT MODIFICADO PARA SER FLEXIBLE
    @GET("evaluations/")
    suspend fun getUserEvaluations(@Header("Authorization") token: String): JsonElement

    // Método de depuración (opcional, se puede mantener o eliminar)
    @GET("auth/profile/")
    suspend fun getUserProfileRaw(@Header("Authorization") token: String): Response<ResponseBody>
}
