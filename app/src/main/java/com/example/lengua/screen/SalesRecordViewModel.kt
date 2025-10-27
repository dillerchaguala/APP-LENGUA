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

// A data class to hold the sales statistics
data class SalesStats(
    val totalSales: Int = 0,
    val totalRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val pendingSales: Int = 0
)

class SalesRecordViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _stats = MutableStateFlow(SalesStats())
    val stats: StateFlow<SalesStats> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // In the future, you would load the initial stats here
        // loadSalesStats()
    }

    fun loadSalesStats(filters: Map<String, String> = emptyMap()) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Here you would call your repository to get the sales data from the API
                // For now, we'll just use dummy data after a delay
                kotlinx.coroutines.delay(1000)
                _stats.value = SalesStats(totalSales = 120, totalRevenue = 2500.00, monthlyRevenue = 800.50, pendingSales = 5)

            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class SalesRecordViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesRecordViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            return SalesRecordViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
