package com.example.lengua.screen

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lengua.R
import com.example.lengua.data.repository.AuthRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.navigation.BottomBarScreen
import com.example.lengua.navigation.BottomNavGraph
import com.example.lengua.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Modelo de Estado y ViewModel (Se mantiene aquí porque es el padre de las pantallas de la barra inferior) ---

data class UserState(
    val id: Int = 0,
    val fullName: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val country: String = "",
    val city: String = "",
    val role: String = "",
    val englishLevel: String = "",
    val profileImageUrl: String? = null,
    val candies: Int = 0,
    val xp: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _userState = MutableStateFlow(UserState(isLoading = true))
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _userState.value = _userState.value.copy(isLoading = true)
            when (val result = authRepository.getUserProfile()) {
                is Result.Success -> {
                    val user = result.data
                    _userState.value = UserState(
                        id = user.id,
                        fullName = user.fullName,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        phone = user.phone ?: "",
                        country = user.country ?: "",
                        city = user.city ?: "",
                        role = user.role ?: "",
                        englishLevel = user.englishLevel ?: "",
                        profileImageUrl = null, // TODO: Obtener del backend
                        candies = 42, // TODO: Obtener del backend
                        xp = 1250, // TODO: Obtener del backend
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _userState.value = UserState(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val sessionManager = SessionManager(context.applicationContext)
            val authRepository = AuthRepository(RetrofitInstance.api, sessionManager)
            return HomeViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val fredokaFont = FontFamily(Font(R.font.fredoka_one))

// --- El nuevo HomeScreen, que actúa como un contenedor para la navegación inferior ---
@Composable
fun HomeScreen(mainNavController: NavController) {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current))
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController = bottomNavController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val userState by homeViewModel.userState.collectAsState()

            BottomNavGraph(
                bottomNavController = bottomNavController,
                mainNavController = mainNavController,
                userState = userState,
                onLogout = {
                    homeViewModel.logout()
                    mainNavController.navigate("main_screen") {
                        popUpTo(mainNavController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Classes,
        BottomBarScreen.Security,
        BottomBarScreen.Fitness,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(containerColor = Color.White) {
        screens.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { 
                    Icon(
                        imageVector = screen.icon, 
                        contentDescription = screen.title,
                        // Cambia el color si el ítem está seleccionado
                        tint = if (isSelected) Color(0xFF6A1B9A) else Color.Gray
                    )
                }
            )
        }
    }
}