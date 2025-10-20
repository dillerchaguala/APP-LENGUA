package com.example.lengua.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lengua.data.repository.AuthRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.network.ApiService
import com.example.lengua.network.Evaluation
import com.example.lengua.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- UI State ---
data class EvaluationsUiState(
    val evaluations: List<Evaluation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// --- ViewModel ---
class EvaluationsViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EvaluationsUiState(isLoading = true))
    val uiState: StateFlow<EvaluationsUiState> = _uiState.asStateFlow()

    init {
        fetchEvaluations()
    }

    fun fetchEvaluations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = authRepository.getUserEvaluations()) {
                is Result.Success -> {
                    _uiState.value = EvaluationsUiState(
                        evaluations = result.data,
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = EvaluationsUiState(error = result.message, isLoading = false)
                }
            }
        }
    }
}

// --- ViewModel Factory ---
class EvaluationsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EvaluationsViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            val authRepository = AuthRepository(RetrofitInstance.api, sessionManager)
            return EvaluationsViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
