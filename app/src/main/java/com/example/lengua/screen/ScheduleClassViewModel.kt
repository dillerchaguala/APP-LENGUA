package com.example.lengua.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.network.CreateClassRequest
import com.example.lengua.network.Professor
import com.example.lengua.network.RetrofitInstance
import com.example.lengua.network.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// --- Funciones de formato de fecha y hora ---
private fun formatDateForBackend(dateString: String): String {
    if (dateString.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
        return dateString
    }
    return try {
        val inputFormat = SimpleDateFormat("dd 'de' MMMM yyyy", Locale("es", "ES"))
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputFormat.parse(dateString)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString // Devolver original si falla la conversión
    }
}

private fun formatTimeForBackend(timeString: String): String {
    if (timeString.matches(Regex("\\d{2}:\\d{2}"))) {
        return timeString
    }
    return try {
        val inputFormat = SimpleDateFormat("h:mm a", Locale.US)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.US)
        val time = inputFormat.parse(timeString)
        outputFormat.format(time!!)
    } catch (e: Exception) {
        timeString // Devolver original si falla la conversión
    }
}

class ScheduleClassViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _professors = MutableStateFlow<List<Professor>>(emptyList())
    val professors: StateFlow<List<Professor>> = _professors

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = sessionManager.getToken()
                if (token != null) {
                    _professors.value = RetrofitInstance.api.getProfessors("Bearer $token").professors
                    _students.value = RetrofitInstance.api.getStudents("Bearer $token").students
                } else {
                    _error.value = "Authentication token not found."
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createClass(
        nombre: String,
        fecha: String,
        hora: String,
        descripcion: String,
        meetLink: String,
        profesor: String,
        estudiantesIds: List<Int>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = false

            try {
                val token = sessionManager.getToken()
                if (token != null) {
                    // ✅ Convertir fecha y hora al formato correcto
                    val fechaFormateada = formatDateForBackend(fecha)
                    val horaFormateada = formatTimeForBackend(hora)

                    val request = CreateClassRequest(
                        nombre = nombre,
                        fecha = fechaFormateada,  // ← Usar fecha formateada
                        hora = horaFormateada,    // ← Usar hora formateada
                        duracion = 60, // Dummy data
                        tema = "General", // Dummy data
                        modalidad = "virtual",
                        descripcion = descripcion,
                        meetLink = meetLink,
                        estudiantes = estudiantesIds,
                        profesor = profesor
                    )
                    val response = RetrofitInstance.api.createClass("Bearer $token", request)
                    if (response.success) {
                        _success.value = true
                    } else {
                        _error.value = response.message ?: "An unknown error occurred."
                    }
                } else {
                     _error.value = "Authentication token not found."
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class ScheduleClassViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleClassViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            return ScheduleClassViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
