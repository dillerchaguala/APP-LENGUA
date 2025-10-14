package com.example.lengua.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lengua.data.repository.AuthRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.network.Evaluation
import com.example.lengua.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// --- Estado, ViewModel y Factory ---
data class EvaluationsUiState(
    val isLoading: Boolean = true,
    val evaluations: List<Evaluation> = emptyList(),
    val error: String? = null
)

class EvaluationsViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(EvaluationsUiState())
    val uiState: StateFlow<EvaluationsUiState> = _uiState.asStateFlow()

    // Se elimina el bloque init para que la carga sea controlada por la UI

    fun loadEvaluations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.getUserEvaluations()) {
                is Result.Success -> {
                    _uiState.value = EvaluationsUiState(isLoading = false, evaluations = result.data)
                }
                is Result.Error -> {
                    _uiState.value = EvaluationsUiState(isLoading = false, error = result.message)
                }
            }
        }
    }
}

class EvaluationsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EvaluationsViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            val authRepository = AuthRepository(RetrofitInstance.api, sessionManager)
            return EvaluationsViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for Evaluations")
    }
}

// --- Pantalla Principal de Evaluaciones ---

@Composable
fun EvaluationsScreen(viewModel: EvaluationsViewModel = viewModel(factory = EvaluationsViewModelFactory(LocalContext.current))) {
    val uiState by viewModel.uiState.collectAsState()

    // ✅ SOLUCIÓN: Forzar la recarga de datos cada vez que la pantalla se muestra.
    LaunchedEffect(Unit) {
        viewModel.loadEvaluations()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
            uiState.evaluations.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No tienes evaluaciones asignadas.", textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.evaluations, key = { it.id }) { evaluation ->
                        EvaluationCard(evaluation = evaluation)
                    }
                }
            }
        }
    }
}

private fun formatIsoDate(isoDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(isoDate) ?: return isoDate
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        isoDate // Fallback al texto original si falla el parseo
    }
}

@Composable
fun EvaluationCard(evaluation: Evaluation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = evaluation.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                evaluation.calificacion?.let {
                    Text(
                        text = it.toInt().toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (it >= 60) Color(0xFF388E3C) else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            evaluation.descripcion?.let {
                if (it.isNotBlank()) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Text("Profesor: ${evaluation.profesor}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("Fecha Límite: ${formatIsoDate(evaluation.fechaLimite)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(text = evaluation.estadoEstudiante)
                TypeBadge(text = evaluation.tipo)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { /* TODO: Lógica de descarga */ },
                    modifier = Modifier.weight(1f),
                    enabled = !evaluation.archivoUrl.isNullOrEmpty()
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Descargar", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Descargar")
                }
                Button(
                    onClick = { /* TODO: Lógica de subida */ },
                    modifier = Modifier.weight(1f),
                    enabled = evaluation.estadoEstudiante.equals("pendiente", ignoreCase = true)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Subir Respuesta", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subir")
                }
            }
        }
    }
}

@Composable
fun StatusBadge(text: String) {
    val color = when (text.lowercase()) {
        "pendiente" -> MaterialTheme.colorScheme.errorContainer
        "entregada" -> Color(0xFFC8E6C9)
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = when (text.lowercase()) {
        "pendiente" -> MaterialTheme.colorScheme.onErrorContainer
        "entregada" -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    )
}

@Composable
fun TypeBadge(text: String) {
    val color = when (text.lowercase()) {
        "quiz" -> Color(0xFFE3F2FD)
        "examen" -> Color(0xFFFFEBEE)
        "tarea" -> Color(0xFFF1F8E9)
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val textColor = when (text.lowercase()) {
        "quiz" -> Color(0xFF1565C0)
        "examen" -> Color(0xFFC62828)
        "tarea" -> Color(0xFF558B2F)
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    )
}
