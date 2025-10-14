package com.example.lengua.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lengua.data.repository.AuthRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Modelo de Estado --- //

data class LoginState(
    val isLoading: Boolean = false,
    val loginResult: Result<String>? = null // Puede ser Success o Error
)

// --- ViewModel y Factory --- //

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            // 1. Inicia el estado de carga
            _uiState.value = LoginState(isLoading = true)
            
            // 2. Llama al repositorio
            val result = authRepository.login(username, password)
            
            // 3. Actualiza el estado con el resultado (éxito o error)
            _uiState.value = LoginState(isLoading = false, loginResult = result)
        }
    }
}

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            val authRepository = AuthRepository(RetrofitInstance.api, sessionManager)
            return LoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}