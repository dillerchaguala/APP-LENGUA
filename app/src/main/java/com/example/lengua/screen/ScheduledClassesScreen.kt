package com.example.lengua.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import com.example.lengua.network.Clase
import com.example.lengua.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClassesUiState(
    val isLoading: Boolean = true,
    val ongoingClasses: List<Clase> = emptyList(),
    val upcomingClasses: List<Clase> = emptyList(),
    val completedClasses: List<Clase> = emptyList(),
    val error: String? = null
)

class ClassesViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ClassesUiState())
    val uiState: StateFlow<ClassesUiState> = _uiState.asStateFlow()

    fun loadUserClasses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.getUserClasses()) {
                is Result.Success -> {
                    val allClasses = result.data
                    _uiState.value = ClassesUiState(
                        isLoading = false,
                        ongoingClasses = allClasses.filter { it.estado.equals("activa", ignoreCase = true) },
                        upcomingClasses = allClasses.filter { it.estado.equals("programada", ignoreCase = true) },
                        completedClasses = allClasses.filter { !it.estado.equals("activa", ignoreCase = true) && !it.estado.equals("programada", ignoreCase = true) }
                    )
                }
                is Result.Error -> _uiState.value = ClassesUiState(isLoading = false, error = result.message)
            }
        }
    }
}

class ClassesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClassesViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            return ClassesViewModel(AuthRepository(RetrofitInstance.api, sessionManager)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for Classes")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledClassesScreen(
    viewModel: ClassesViewModel = viewModel(factory = ClassesViewModelFactory(LocalContext.current)),
    onMenuClick: () -> Unit // ✅ PARÁMETRO AÑADIDO
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadUserClasses() }

    // ✅ SCAFFOLD Y TOPAPPBAR RESTAURADOS
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clases Programadas") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                uiState.ongoingClasses.isEmpty() && uiState.upcomingClasses.isEmpty() && uiState.completedClasses.isEmpty() -> Text("No tienes clases para mostrar.", textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                else -> ClassesList(uiState.ongoingClasses, uiState.upcomingClasses, uiState.completedClasses)
            }
        }
    }
}

@Composable
fun ClassesList(ongoing: List<Clase>, upcoming: List<Clase>, completed: List<Clase>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (ongoing.isNotEmpty()) {
            item { SectionHeader("En Curso") }
            items(ongoing, key = { it.id }) { ClassCard(it) }
        }
        if (upcoming.isNotEmpty()) {
            item { SectionHeader("Próximas") }
            items(upcoming, key = { it.id }) { ClassCard(it) }
        }
        if (completed.isNotEmpty()) {
            item { SectionHeader("Historial (Completadas)") }
            items(completed, key = { it.id }) { ClassCard(it) }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
}

@Composable
fun ClassCard(clase: Clase) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1.5f)) { Text(clase.fecha, fontSize = 14.sp); Text(clase.hora, fontSize = 12.sp, color = Color.Gray) }
            Text(clase.profesor, modifier = Modifier.weight(1.5f), fontSize = 14.sp)
            Text(clase.tema, modifier = Modifier.weight(1.5f), fontSize = 14.sp)
            Box(modifier = Modifier.weight(1.5f), contentAlignment = Alignment.Center) { StatusChip(clase.estado) }
        }
        HorizontalDivider()
    }
}

@Composable
fun StatusChip(status: String) {
    val (color, textColor) = when (status.lowercase()) {
        "activa" -> Color(0xFF388E3C) to Color.White
        "programada" -> Color(0xFF0288D1) to Color.White
        "completada" -> Color.Gray to Color.White
        "cancelada" -> Color(0xFFD32F2F) to Color.White
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(color).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(status.replaceFirstChar { it.uppercase() }, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
