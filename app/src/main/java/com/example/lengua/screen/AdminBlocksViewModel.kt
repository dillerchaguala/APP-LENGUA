package com.example.lengua.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lengua.data.model.Bloque
import com.example.lengua.data.repository.AuthRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- UI State ---
data class AdminBlocksUiState(
    val blocksByLevel: Map<String, List<Bloque>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// --- ViewModel ---
class AdminBlocksViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminBlocksUiState(isLoading = true))
    val uiState: StateFlow<AdminBlocksUiState> = _uiState.asStateFlow()

    // ✅ ESTADO PARA MANEJAR EL DIÁLOGO
    private val _selectedBlock = MutableStateFlow<Bloque?>(null)
    val selectedBlock: StateFlow<Bloque?> = _selectedBlock.asStateFlow()

    init {
        fetchBlocks()
    }

    fun fetchBlocks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = authRepository.getBloques()) {
                is Result.Success -> {
                    _uiState.value = AdminBlocksUiState(
                        blocksByLevel = result.data.groupBy { it.nivel }.toSortedMap(),
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = AdminBlocksUiState(error = result.message, isLoading = false)
                }
                is Result.Loading -> {
                    // Keep loading state
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    // ✅ FUNCIONES PARA CONTROLAR EL DIÁLOGO
    fun onBlockSelected(block: Bloque) {
        _selectedBlock.value = block
    }

    fun onDialogDismiss() {
        _selectedBlock.value = null
    }
    
    // TODO: Add functions for create, update, and delete blocks.
}

// --- ViewModel Factory ---
class AdminBlocksViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminBlocksViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            val authRepository = AuthRepository(RetrofitInstance.api, sessionManager)
            return AdminBlocksViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
