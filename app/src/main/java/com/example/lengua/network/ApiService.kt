package com.example.lengua.network

import com.example.lengua.data.LoginRequest
import com.example.lengua.data.LoginResponse
import com.example.lengua.data.model.BloquesResponse
import com.example.lengua.data.model.CreateUserRequest
import com.example.lengua.data.model.CreateUserResponse
import com.example.lengua.data.model.EspecializacionesResponse
import com.example.lengua.data.model.User as UserModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.Response
import retrofit2.http.Path

// --- Modelos de Datos para el Perfil ---
@Serializable
data class UserProfileResponse(val success: Boolean, val user: User)

@Serializable
data class UpdateProfileResponse(val success: Boolean, val user: User)

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
data class ClassesResponse(val success: Boolean, val total: Int, val clases: List<Clase>)

@Serializable
data class Clase(
    val id: Int, val nombre: String, val profesor: String, val fecha: String, val hora: String,
    val duracion: Int, val tema: String, val descripcion: String? = null,
    @SerialName("tipo_clase") val tipoClase: String, val modalidad: String,
    @SerialName("meet_link") val meetLink: String? = null, val estado: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CreateClassRequest(
    val nombre: String,
    val fecha: String,  // "YYYY-MM-DD"
    val hora: String,   // "HH:MM"
    val duracion: Int,
    val tema: String,
    val modalidad: String,  // "virtual" o "presencial"
    val descripcion: String = "",
    @SerialName("tipo_clase")
    val tipoClase: String = "individual",
    @SerialName("meet_link")
    val meetLink: String = "",
    val estudiantes: List<Int> = emptyList(),
    val profesor: String? = null // For admins to specify
)

@Serializable
data class CreateClassResponse(
    val success: Boolean,
    val message: String? = null,
    val clase: Clase? = null,
    val errors: Map<String, List<String>>? = null
)

// --- Modelos de Datos para las Evaluaciones ---
@Serializable
data class EvaluationsResponse(val success: Boolean, val total: Int, val evaluaciones: List<Evaluation>, val message: String? = null)

@Serializable
data class Evaluation(
    val id: Int, val titulo: String, val descripcion: String?, val tipo: String, val profesor: String,
    @SerialName("fecha_limite") val fechaLimite: String, @SerialName("archivo_url") val archivoUrl: String?,
    @SerialName("estado_estudiante") val estadoEstudiante: String, @SerialName("fecha_entrega") val fechaEntrega: String?,
    val calificacion: Float?, @SerialName("created_at") val createdAt: String
)

// --- Modelos de Datos para los Bloques (Create / Detail) ---
@Serializable
data class BloqueDetailResponse(val success: Boolean, val bloque: com.example.lengua.data.model.Bloque)

@Serializable
data class BloqueCreateRequest(
    val nombre: String, val nivel: String, @SerialName("grupo_color") val grupoColor: String,
    @SerialName("horario_inicio") val horarioInicio: String? = null, @SerialName("horario_fin") val horarioFin: String? = null,
    @SerialName("cupo_maximo") val cupoMaximo: Int = 20, val estado: String = "configurado", val activo: Boolean = true
)

// --- MODELOS DE DATOS PARA CLUBS ---
@Serializable
data class ClubsResponse(
    val success: Boolean = false,
    val total: Int = 0,
    val clubs: List<Club> = emptyList(),
    val message: String? = null
)

@Serializable
data class Club(
    val id: Int,
    val name: String,
    val description: String,
    val profesor: String,
    @SerialName("total_students") val totalStudents: Int,
    val materials: List<ClubMaterial>,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class ClubMaterial(
    val id: Int,
    val week: String,
    val title: String,
    val description: String,
    @SerialName("resource_type") val resourceType: String,
    val url: String,
    @SerialName("file_url") val fileUrl: String,
    @SerialName("created_at") val createdAt: String
)

// --- Modelos de Datos para Profesores y Estudiantes ---
@Serializable
data class Professor(
    val id: Int,
    val username: String,
    val email: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("full_name") val fullName: String
)

@Serializable
data class ProfessorsResponse(
    val success: Boolean,
    val total: Int,
    val professors: List<Professor>
)

@Serializable
data class Student(
    val id: Int,
    val username: String,
    val email: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("full_name") val fullName: String
)

@Serializable
data class StudentsResponse(
    val success: Boolean,
    val total: Int,
    val students: List<Student>
)

// --- Interfaz del Servicio API ---
interface ApiService {
    @POST("login/")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("auth/profile/")
    suspend fun getUserProfile(@Header("Authorization") token: String): UserProfileResponse

    @PUT("profile/update/")
    suspend fun updateUserProfile(@Header("Authorization") token: String, @Body profileData: Map<String, String>): UpdateProfileResponse

    @GET("classes/")
    suspend fun getUserClasses(@Header("Authorization") token: String): ClassesResponse

    @GET("evaluations/")
    suspend fun getUserEvaluations(@Header("Authorization") token: String): JsonElement

    @GET("bloques/")
    suspend fun getBloques(@Header("Authorization") token: String): Response<BloquesResponse>
    
    @GET("especializaciones/activas/")
    suspend fun getEspecializaciones(@Header("Authorization") token: String): Response<EspecializacionesResponse>

    @GET("clubs/")
    suspend fun getUserClubs(@Header("Authorization") token: String): ClubsResponse

    @POST("bloques/create/")
    suspend fun createBloque(@Header("Authorization") token: String, @Body bloque: BloqueCreateRequest): BloqueDetailResponse

    @GET("bloques/{id}/")
    suspend fun getBloqueDetail(@Header("Authorization") token: String, @Path("id") id: Int): BloqueDetailResponse

    @PUT("bloques/{id}/update/")
    suspend fun updateBloque(@Header("Authorization") token: String, @Path("id") id: Int, @Body bloque: BloqueCreateRequest): BloqueDetailResponse

    @POST("bloques/{id}/toggle/")
    suspend fun toggleBloque(@Header("Authorization") token: String, @Path("id") id: Int): BloqueDetailResponse

    @POST("bloques/{id}/delete/")
    suspend fun deleteBloque(@Header("Authorization") token: String, @Path("id") id: Int): Response<Unit>

    @POST("classes/create/")
    suspend fun createClass(
        @Header("Authorization") token: String,
        @Body request: CreateClassRequest
    ): CreateClassResponse

    @GET("professors/")
    suspend fun getProfessors(@Header("Authorization") token: String): ProfessorsResponse

    @GET("students/")
    suspend fun getStudents(@Header("Authorization") token: String): StudentsResponse

    // User Management Endpoints
    @GET("users/")
    suspend fun getUsers(
        @Header("Authorization") token: String
    ): Response<List<UserModel>>

    @POST("auth/register/")
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Body request: CreateUserRequest
    ): Response<CreateUserResponse>

    @POST("users/{id}/toggle-active/")
    suspend fun toggleUserActive(
        @Header("Authorization") token: String,
        @Path("id") userId: Int
    ): Response<ApiResponse<UserModel>>
}

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)
