package com.example.lengua.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lengua.R
import com.example.lengua.data.repository.AuthRepository
import com.example.lengua.data.repository.Result
import com.example.lengua.data.repository.SessionManager
import com.example.lengua.network.RetrofitInstance
import com.example.lengua.network.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Modelo de Estado y ViewModel ---

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

    fun loadUserProfile() {
        viewModelScope.launch {
            _userState.value = UserState(isLoading = true) // Reiniciar estado al cargar

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
                        // TODO: Obtener estos datos del backend
                        profileImageUrl = null, // user.profileImageUrl,
                        candies = 42, // user.candies,
                        xp = 1250, // user.xp,
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


// --- Composable Principal ---

val fredokaFont = FontFamily(Font(R.font.fredoka_one))

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current))
) {
    val userState by homeViewModel.userState.collectAsState()

    when {
        userState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        userState.error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ocurrió un error",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userState.error!!,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { homeViewModel.loadUserProfile() }) {
                    Text("Reintentar")
                }
            }
        }
        else -> {
            MainContent(
                userState = userState,
                navController = navController, // Se pasa el NavController
                onLogout = {
                    homeViewModel.logout()
                    navController.navigate("main_screen") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(userState: UserState, navController: NavController, onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                email = userState.email,
                profileImageUrl = userState.profileImageUrl,
                onProfileClicked = { navController.navigate("profile_screen") }, // <-- NAVEGACIÓN AÑADIDA
                onSettingsClicked = { /*TODO*/ },
                onLogoutClicked = onLogout,
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Lengua", fontFamily = fredokaFont, color = Color(0xFF6A1B9A)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        StatsCounter(candies = userState.candies, xp = userState.xp)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                )
            },
            bottomBar = { BottomNavigationBar() }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { WelcomeBanner(name = userState.firstName) }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item { AvatarCard() }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item { ProgressCard() }
                item { Spacer(modifier = Modifier.height(24.dp)) }
                item { DailyChallengeCard() }
                item { Spacer(modifier = Modifier.height(32.dp)) }
                item {
                    Text(
                        text = "MISIONES",
                        color = Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fredokaFont
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { MissionCard(
                    color1 = Color(0xFF6A1B9A),
                    color2 = Color(0xFF42A5F5),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Gramática pasado",
                    subtitle = "Quiz interactivo",
                    points = 1250,
                    starColor = Color(0xFFFFD600)
                ) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { MissionCard(
                    color1 = Color(0xFFEF6C00),
                    color2 = Color(0xFF66BB6A),
                    icon = Icons.Default.Edit,
                    title = "Gramática pasado",
                    subtitle = "Quiz interactivo",
                    points = 1250,
                    starColor = Color(0xFFFFD600)
                ) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { MissionCard(
                    color1 = Color(0xFFFFA726),
                    color2 = Color(0xFF26C6DA),
                    icon = Icons.Default.Cloud,
                    title = "Ir de Compras",
                    subtitle = "Jugar en tiempo real",
                    points = 1250,
                    starColor = Color(0xFFFFD600)
                ) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// --- Sub-composables (sin cambios) ---

@Composable
fun StatsCounter(candies: Int, xp: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF7C4DFF), Color(0xFFE040FB))
                )
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Dulces",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = candies.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = fredokaFont
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier
                .height(16.dp)
                .width(1.dp),
            color = Color.White.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "XP",
            tint = Color(0xFFFFD600),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$xp XP",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = fredokaFont
        )
    }
}

@Composable
fun DrawerHeader(email: String, profileImageUrl: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar de usuario",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF6A1B9A)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = email,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    email: String,
    profileImageUrl: String?,
    onProfileClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet {
        DrawerHeader(email = email, profileImageUrl = profileImageUrl)
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(12.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = false,
            onClick = {
                onProfileClicked()
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Configuraciones") },
            label = { Text("Configuraciones") },
            selected = false,
            onClick = {
                onSettingsClicked()
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cierre de sesión") },
            label = { Text("Cierre de sesión") },
            selected = false,
            onClick = {
                onLogoutClicked()
                closeDrawer()
            }
        )
    }
}

@Composable
fun WelcomeBanner(name: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Button(
            onClick = { },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C896)),
            modifier = Modifier
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = if (name.isNotEmpty()) "Welcome $name" else "Welcome",
                modifier = Modifier.padding(start = 24.dp, top = 8.dp, end = 72.dp, bottom = 8.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fredokaFont
            )
        }
    }
}

@Composable
fun AvatarCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A00E0))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tu avatar", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Default.Person, contentDescription = "Person icon", tint = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Personaliza tu avatar para acompañar a Lingo en su viaje migratorio.", color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar", tint = Color.Cyan, modifier = Modifier.size(60.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.3f))
            ) {
                Text("Personalizar", color = Color.White)
            }
        }
    }
}

@Composable
fun ProgressCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00C896))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tu progreso", color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Default.Assessment, contentDescription = "Graph icon", tint = Color.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nivel actual: principiante", color = Color.Black.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun DailyChallengeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF59D))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Reto diario", color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = fredokaFont)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.Default.Whatshot, contentDescription = "Fire icon", tint = Color(0xFFFF5722), modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFBDBDBD), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF7043), Color(0xFFFFC107))))
            ) {
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("¡En racha!", fontFamily = fredokaFont, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dia 0 de racha", fontWeight = FontWeight.Bold, fontFamily = fredokaFont, color = Color.Black, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Completa el reto de hoy para mantener tu racha invicta", color = Color.Black, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                repeat(5) { i ->
                    Box(
                        modifier = Modifier
                            .size(32.dp, 10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .then(if (i == 0) Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFFF7043), Color(0xFFFFC107)))) else Modifier.background(Color(0xFFBDBDBD)))
                    )
                    if (i < 4) Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Proxima recompensa 50 puntos", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = fredokaFont)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF7043), Color(0xFFFFC107))))
            ) {
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("Ver detalles", fontFamily = fredokaFont, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MissionCard(
    color1: Color,
    color2: Color,
    icon: ImageVector,
    title: String,
    subtitle: String,
    points: Int,
    starColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier.background(Brush.verticalGradient(listOf(color1, color2)))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = fredokaFont)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, fontFamily = fredokaFont)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Completa esta misión para avanzar en tu aprendizaje", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(points.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = fredokaFont)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Star, contentDescription = "Star", tint = starColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Jugar ahora", color = Color.White, fontFamily = fredokaFont, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun BottomNavigationBar() {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = Color(0xFFD32F2F)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Book", tint = Color(0xFF7C4DFF)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(imageVector = Icons.Default.Security, contentDescription = "Shield", tint = Color(0xFF388E3C)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = "Dumbbell", tint = Color(0xFF26A69A)) }
        )
    }
}
