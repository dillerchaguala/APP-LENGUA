package com.example.lengua.screen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lengua.data.repository.AuthRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.network.RetrofitInstance
import com.example.lengua.network.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Data & ViewModel (sin cambios) ---
data class UserState(val isLoading: Boolean = false, val error: String? = null, val email: String = "", val firstName: String = "")
class HomeViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _userState = MutableStateFlow(UserState(isLoading = true))
    val userState: StateFlow<UserState> = _userState.asStateFlow()
    init { loadUserProfile() }
    fun loadUserProfile() {
        viewModelScope.launch {
            _userState.value = UserState(isLoading = true)
            when (val result = authRepository.getUserProfile()) {
                is Result.Success -> {
                    val user = result.data
                    _userState.value = UserState(isLoading = false, email = user.email, firstName = user.firstName)
                }
                is Result.Error -> _userState.value = UserState(isLoading = false, error = result.message)
            }
        }
    }
    fun logout() { authRepository.logout() }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") // ✅ ANOTACIÓN AÑADIDA
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val authRepository = AuthRepository(RetrofitInstance.api, SessionManager(context.applicationContext))
            return HomeViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- Definiciones de Navegación ---
sealed class BottomBarScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomBarScreen("home", "Lengua", Icons.Default.Home)
    object Classes : BottomBarScreen("classes", "Clases", Icons.AutoMirrored.Filled.MenuBook)
    object Security : BottomBarScreen("security", "Security", Icons.Default.Security)
    object Progress : BottomBarScreen("progress", "Progress", Icons.Default.FitnessCenter)
}
val bottomBarScreens = listOf(BottomBarScreen.Home, BottomBarScreen.Classes, BottomBarScreen.Security, BottomBarScreen.Progress)

// --- PUNTO DE ENTRADA PRINCIPAL: Contiene el Scaffold con la barra inferior ---
@Composable
fun HomeScreen(mainNavController: NavController, homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current))) {
    val userState by homeViewModel.userState.collectAsState()
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = bottomNavController) }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomBarScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomBarScreen.Home.route) {
                HomeTabScreen(
                    userState = userState,
                    mainNavController = mainNavController,
                    onLogout = { homeViewModel.logout(); mainNavController.navigate("main_screen") { popUpTo(mainNavController.graph.startDestinationId) { inclusive = true } } }
                )
            }
            composable(BottomBarScreen.Classes.route) {
                LearningSectionScreen()
            }
            composable(BottomBarScreen.Security.route) { PlaceholderScreen("Security") }
            composable(BottomBarScreen.Progress.route) { PlaceholderScreen("Progress") }
        }
    }
}

// --- PANTALLA DE LA PESTAÑA "HOME": CON SU PROPIO MENÚ ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabScreen(userState: UserState, mainNavController: NavController, onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { AppDrawer(email = userState.email, onProfileClicked = { mainNavController.navigate("profile_screen") }, onLogoutClicked = onLogout, closeDrawer = { scope.launch { drawerState.close() } }) }
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Lengua") }, navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu") } }) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                HomeDashboardScreen(userState = userState, navController = mainNavController)
            }
        }
    }
}

// --- Menú lateral para la pestaña "Home" ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(email: String, onProfileClicked: () -> Unit, onLogoutClicked: () -> Unit, closeDrawer: () -> Unit) {
    ModalDrawerSheet {
        DrawerHeader(email = email)
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(12.dp))
        NavigationDrawerItem(icon = { Icon(Icons.Default.Person, "Perfil") }, label = { Text("Perfil") }, selected = false, onClick = { onProfileClicked(); closeDrawer() })
        NavigationDrawerItem(icon = { Icon(Icons.Default.Settings, "Configuraciones") }, label = { Text("Configuraciones") }, selected = false, onClick = closeDrawer)
        NavigationDrawerItem(icon = { Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión") }, label = { Text("Cerrar sesión") }, selected = false, onClick = { onLogoutClicked(); closeDrawer() })
    }
}

@Composable
fun DrawerHeader(email: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.AccountCircle, "Avatar", modifier = Modifier.size(80.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        Text(email, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// --- Barra de Navegación Inferior (Compartida) ---
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar(containerColor = Color.White) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomBarScreens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigate(screen.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                icon = { Icon(screen.icon, contentDescription = screen.title, tint = if (selected) Color(0xFF6A1B9A) else Color.Gray) }
            )
        }
    }
}

// --- Pantalla de ejemplo ---
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Pantalla de $title")
    }
}
