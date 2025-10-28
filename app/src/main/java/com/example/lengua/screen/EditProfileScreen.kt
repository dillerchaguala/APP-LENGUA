package com.example.lengua.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

// --- Estado de la UI para la pantalla de edición ---
data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val country: String = "",
    val city: String = "",
    val address: String = "",
    val correoPersonal: String = "",
    // Campos no editables que podríamos necesitar mostrar
    val email: String = "",
    val role: String = ""
)

// --- ViewModel para la pantalla de edición ---
class EditProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadInitialProfile()
    }

    private fun loadInitialProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.getUserProfile()) {
                is Result.Success -> {
                    val user = result.data
                    _uiState.value = EditProfileUiState(
                        firstName = user.firstName,
                        lastName = user.lastName,
                        phone = user.phone,
                        country = user.country,
                        city = user.city,
                        address = user.address,
                        correoPersonal = user.correoPersonal,
                        email = user.email,
                        role = user.role
                    )
                }
                is Result.Error -> _error.value = result.message
                is Result.Loading -> _isLoading.value = true
            }
            _isLoading.value = false
        }
    }

    fun onFirstNameChange(value: String) { _uiState.value = _uiState.value.copy(firstName = value) }
    fun onLastNameChange(value: String) { _uiState.value = _uiState.value.copy(lastName = value) }
    fun onPhoneChange(value: String) { _uiState.value = _uiState.value.copy(phone = value) }
    fun onCountryChange(value: String) { _uiState.value = _uiState.value.copy(country = value) }
    fun onCityChange(value: String) { _uiState.value = _uiState.value.copy(city = value) }
    fun onAddressChange(value: String) { _uiState.value = _uiState.value.copy(address = value) }
    fun onCorreoPersonalChange(value: String) { _uiState.value = _uiState.value.copy(correoPersonal = value) }

    fun saveProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val currentState = _uiState.value
            val profileData = mapOf(
                "first_name" to currentState.firstName,
                "last_name" to currentState.lastName,
                "phone" to currentState.phone,
                "country" to currentState.country,
                "city" to currentState.city,
                "address" to currentState.address,
                "correo_personal" to currentState.correoPersonal
            )

            when (val result = authRepository.updateUserProfile(profileData)) {
                is Result.Success -> _saveSuccess.value = true
                is Result.Error -> _error.value = result.message
                is Result.Loading -> _isLoading.value = true
            }
            _isLoading.value = false
        }
    }
}

// --- Factory para el ViewModel ---
class EditProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            val authRepository = AuthRepository(RetrofitInstance.api, sessionManager)
            return EditProfileViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for EditProfile")
    }
}

// --- Pantalla Principal ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = viewModel(factory = EditProfileViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // Navegar hacia atrás si el guardado fue exitoso
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveProfile() }, enabled = !isLoading) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            // Campos de texto para editar la información
            EditProfileTextField(label = "Nombre", value = uiState.firstName, onValueChange = viewModel::onFirstNameChange)
            EditProfileTextField(label = "Apellido", value = uiState.lastName, onValueChange = viewModel::onLastNameChange)
            EditProfileTextField(label = "Teléfono", value = uiState.phone, onValueChange = viewModel::onPhoneChange)
            EditProfileTextField(label = "País", value = uiState.country, onValueChange = viewModel::onCountryChange)
            EditProfileTextField(label = "Ciudad", value = uiState.city, onValueChange = viewModel::onCityChange)
            EditProfileTextField(label = "Dirección", value = uiState.address, onValueChange = viewModel::onAddressChange)
            EditProfileTextField(label = "Correo Personal", value = uiState.correoPersonal, onValueChange = viewModel::onCorreoPersonalChange)
        }
    }
}

@Composable
private fun EditProfileTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}
