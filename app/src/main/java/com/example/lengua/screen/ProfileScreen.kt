package com.example.lengua.screen

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lengua.data.repository.AuthRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Modelo de Estado, ViewModel y Factory ---

data class ProfileUserState(
    val fullName: String = "Usuario",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val country: String = "",
    val city: String = "",
    val role: String = "Estudiante",
    val englishLevel: String = "",
    val birthDate: String = "",
    val address: String = "",
    val learningGoals: String = "",
    val profileCompleted: Boolean = false,
    val bloqueAsignado: String = "",
    val createdAt: String = "",
    val correoPersonal: String = ""
)

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUserState?>(null)
    val uiState: StateFlow<ProfileUserState?> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.getUserProfile()) {
                is Result.Success -> {
                    val user = result.data
                    _uiState.value = ProfileUserState(
                        fullName = user.fullName.takeIf { it.isNotBlank() } ?: user.username,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        phone = user.phone,
                        country = user.country,
                        city = user.city,
                        role = user.role.replaceFirstChar { it.uppercase() },
                        englishLevel = user.englishLevel,
                        birthDate = user.birthDate,
                        address = user.address,
                        learningGoals = user.learningGoals,
                        profileCompleted = user.profileCompleted,
                        bloqueAsignado = user.bloqueAsignado,
                        createdAt = user.createdAt,
                        correoPersonal = user.correoPersonal
                    )
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message
                }
            }
            _isLoading.value = false
        }
    }
}

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            val authRepository = AuthRepository(RetrofitInstance.api, sessionManager)
            return ProfileViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for Profile")
    }
}

// --- Pantalla Principal y Sub-Secciones ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current))
) {
    val userState by profileViewModel.uiState.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // ✅ LÓGICA CORREGIDA: Recargar SIEMPRE que la pantalla aparezca.
            if (event == Lifecycle.Event.ON_RESUME) {
                profileViewModel.loadUserProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("edit_profile_screen") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Perfil")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Muestra el loading solo la primera vez (cuando el estado es nulo)
                isLoading && userState == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                userState != null -> {
                    // Muestra los datos (incluso si son viejos) mientras se recargan,
                    // evitando que la pantalla parpadee en blanco.
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item { ProfileHeader(userState!!) }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                        item { PersonalInfoSection(userState!!) }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                        item { AcademicInfoSection(userState!!) }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
                // Si no hay estado, ni error, ni está cargando, muestra el spinner como fallback.
                else -> {
                     CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHeader(userState: ProfileUserState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF6A1B9A))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userState.fullName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userState.email,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            SuggestionChip(
                onClick = {},
                label = { Text(userState.role, color = Color.White) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
fun PersonalInfoSection(userState: ProfileUserState) {
    ProfileSection(
        title = "Información Personal",
        icon = Icons.Default.Person
    ) {
        ProfileField("Nombre", userState.firstName.ifEmpty { "No especificado" })
        ProfileField("Apellido", userState.lastName.ifEmpty { "No especificado" })
        ProfileField("Teléfono", userState.phone.ifEmpty { "No especificado" })
        ProfileField("País", userState.country.ifEmpty { "No especificado" })
        ProfileField("Ciudad", userState.city.ifEmpty { "No especificado" })
        ProfileField(
            "Fecha de Nacimiento", 
            userState.birthDate.ifEmpty { "No especificada" }
        )
        ProfileField(
            "Dirección", 
            userState.address.ifEmpty { "No especificada" }
        )
        ProfileField(
            "Correo Personal", 
            userState.correoPersonal.ifEmpty { "No especificado" }
        )
    }
}

@Composable
fun AcademicInfoSection(userState: ProfileUserState) {
    ProfileSection(
        title = "Información Académica",
        icon = Icons.Default.School
    ) {
        ProfileField(
            "Nivel de Inglés", 
            userState.englishLevel.ifEmpty { "No evaluado" }
        )
        ProfileField("Rol", userState.role)
        ProfileField(
            "Bloque Asignado", 
            userState.bloqueAsignado.ifEmpty { "No asignado" }
        )
        ProfileField(
            "Objetivos de Aprendizaje",
            userState.learningGoals.ifEmpty { "No definidos" }
        )
    }
}

@Composable
fun ProfileSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ColumnScope.ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
    Divider()
}
