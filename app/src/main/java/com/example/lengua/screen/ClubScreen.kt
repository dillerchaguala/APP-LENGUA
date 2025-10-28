package com.example.lengua.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lengua.data.repository.AuthRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.network.Club
import com.example.lengua.network.ClubMaterial
import com.example.lengua.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- 1. MODELO DE ESTADO DE LA UI ---
data class ClubsUiState(
    val isLoading: Boolean = false,
    val clubs: List<Club> = emptyList(),
    val selectedClub: Club? = null,
    val error: String? = null
)

// --- 2. VIEWMODEL ---
class ClubsViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ClubsUiState())
    val uiState: StateFlow<ClubsUiState> = _uiState.asStateFlow()

    init {
        loadClubs()
    }

    fun loadClubs() {
        viewModelScope.launch {
            _uiState.value = ClubsUiState(isLoading = true)
            when (val result = repository.getUserClubs()) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        clubs = result.data,
                        selectedClub = result.data.firstOrNull()
                    )
                }
                is Result.Error -> _uiState.value = ClubsUiState(error = result.message)
                is Result.Loading -> { /* El estado ya está en isLoading = true */ }
            }
        }
    }

    fun selectClub(club: Club) {
        _uiState.update { it.copy(selectedClub = club) }
    }
}

// --- 3. VIEWMODEL FACTORY ---
class ClubsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClubsViewModel::class.java)) {
            val repo = RetrofitInstance.getAuthRepository(context.applicationContext)
            return ClubsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- 4. PANTALLA PRINCIPAL DEL CLUB ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubScreen(
    onMenuClick: () -> Unit,
    viewModel: ClubsViewModel = viewModel(factory = ClubsViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Material del Club") }, navigationIcon = { IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menú") } })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                uiState.error != null -> ErrorState(error = uiState.error!!, onRetry = { viewModel.loadClubs() })
                uiState.clubs.isEmpty() -> EmptyState()
                else -> {
                    ClubSelector(clubs = uiState.clubs, selectedClub = uiState.selectedClub, onClubSelected = { viewModel.selectClub(it) })
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.selectedClub?.materials ?: emptyList()) { material ->
                            MaterialCard(material = material)
                        }
                    }
                }
            }
        }
    }
}

// --- 5. COMPONENTES DE LA UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubSelector(clubs: List<Club>, selectedClub: Club?, onClubSelected: (Club) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedClub?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Seleccionar Club") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            clubs.forEach { club ->
                DropdownMenuItem(
                    text = { Text(club.name) },
                    onClick = {
                        onClubSelected(club)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MaterialCard(material: ClubMaterial) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFE3DFFF)).padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Attachment, contentDescription = null, tint = Color(0xFF5A3E9A))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(material.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF3F2D6B))
                        Text("Semana: ${material.week}", fontSize = 14.sp, color = Color(0xFF5A3E9A))
                    }
                    val date = material.createdAt.substringBefore("T")
                    Text(date, modifier = Modifier.background(Color(0xFFD4FADF), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp), color = Color(0xFF006D39), fontSize = 12.sp)
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(material.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        val url = if (material.resourceType == "file") material.fileUrl else material.url
                        if (url.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A63D2))
                ) {
                    Text("Abrir recurso", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No estás asignado a ningún club", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(error, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
