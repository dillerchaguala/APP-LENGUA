package com.example.lengua.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lengua.data.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpecializationsViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _specializations = MutableStateFlow<List<Specialization>>(emptyList())
    val specializations: StateFlow<List<Specialization>> = _specializations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSpecializations()
    }

    fun loadSpecializations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate a network call
                kotlinx.coroutines.delay(1000)
                // In the future, you would fetch this from your repository/API
                _specializations.value = mockSpecializations
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load specializations"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class SpecializationsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpecializationsViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            return SpecializationsViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
