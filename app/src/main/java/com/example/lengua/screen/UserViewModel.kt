package com.example.lengua.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lengua.data.model.Bloque
import com.example.lengua.data.model.CreateUserRequest
import com.example.lengua.data.model.Especializacion
import com.example.lengua.data.model.User
import com.example.lengua.data.repository.FormDataRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.data.repository.UserRepository
import com.example.lengua.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateUserViewModel(
    private val userRepository: UserRepository,
    private val formDataRepository: FormDataRepository
) : ViewModel() {
    
    private val _usersState = MutableStateFlow<Result<List<User>>>(Result.Loading)
    val usersState: StateFlow<Result<List<User>>> = _usersState.asStateFlow()
    
    private val _createUserState = MutableStateFlow<Result<User>?>(null)
    val createUserState: StateFlow<Result<User>?> = _createUserState.asStateFlow()

    // Estados para bloques
    private val _bloquesState = MutableStateFlow<Result<List<Bloque>>>(Result.Loading)
    val bloquesState: StateFlow<Result<List<Bloque>>> = _bloquesState.asStateFlow()
    
    // Estados para especializaciones
    private val _especializacionesState = MutableStateFlow<Result<List<Especializacion>>>(Result.Loading)
    val especializacionesState: StateFlow<Result<List<Especializacion>>> = _especializacionesState.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _usersState.value = Result.Loading
            _usersState.value = userRepository.getUsers()
        }
    }

    fun loadBloques() {
        viewModelScope.launch {
            _bloquesState.value = Result.Loading
            _bloquesState.value = formDataRepository.getBloques()
        }
    }

    fun loadEspecializaciones() {
        viewModelScope.launch {
            _especializacionesState.value = Result.Loading
            _especializacionesState.value = formDataRepository.getEspecializaciones()
        }
    }
    
    fun createUser(
        username: String?,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String,
        bloqueAsignado: String? = null,
        especializacion: String? = null,
        correoPersonal: String? = null
    ) {
        viewModelScope.launch {
            _createUserState.value = Result.Loading
            
            val request = CreateUserRequest(
                username = username,
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                role = role,
                bloqueAsignado = bloqueAsignado,
                especializacion = especializacion,
                correoPersonal = correoPersonal
            )
            
            _createUserState.value = userRepository.createUser(request)
            
            if (_createUserState.value is Result.Success) {
                loadUsers()
            }
        }
    }
    
    fun resetCreateUserState() {
        _createUserState.value = null
    }
}

class CreateUserViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateUserViewModel::class.java)) {
            val apiService = RetrofitInstance.api
            val sessionManager = SessionManager(context.applicationContext)
            val userRepository = UserRepository(apiService, sessionManager)
            val formDataRepository = FormDataRepository(apiService, sessionManager)
            return CreateUserViewModel(userRepository, formDataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
