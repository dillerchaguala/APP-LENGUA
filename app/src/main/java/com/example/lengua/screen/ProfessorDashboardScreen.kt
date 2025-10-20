package com.example.lengua.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

// Data class for Professor menu items
data class ProfessorMenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

// List of Professor menu items based on the image
val professorMenuItems = listOf(
    ProfessorMenuItem("prof_dashboard", "Dashboard", Icons.Default.Dashboard),
    ProfessorMenuItem("my_classes", "Mis clases", Icons.Default.Class),
    ProfessorMenuItem("schedule_classes", "Programar clases", Icons.Default.CalendarToday),
    ProfessorMenuItem("evaluations", "Evaluaciones", Icons.Default.Assessment),
    ProfessorMenuItem("grade_evaluations", "Calificar evaluaciones", Icons.Default.Grading),
    ProfessorMenuItem("progress_reports", "Reportes de progreso", Icons.Default.BarChart),
    ProfessorMenuItem("notifications", "Notificaciones", Icons.Default.Notifications),
    ProfessorMenuItem("students", "Estudiantes", Icons.Default.School)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessorDashboardScreen(onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    var currentTitle by remember { mutableStateOf(professorMenuItems.first().title) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Profile Header
                Column(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Avatar de profesor",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Prof. undefenided", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                HorizontalDivider()

                // Menu Items
                Column(Modifier.weight(1f)) {
                    Spacer(Modifier.height(12.dp))
                    professorMenuItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = item.title == currentTitle,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    popUpTo(navController.graph.startDestinationId)
                                }
                                currentTitle = item.title
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                // Logout Button
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión") },
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentTitle) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavHost(navController = navController, startDestination = professorMenuItems.first().route) {
                    professorMenuItems.forEach { item ->
                        composable(item.route) {
                            ProfessorPlaceholderScreen(title = item.title)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessorPlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Pantalla de $title", style = MaterialTheme.typography.headlineMedium)
    }
}
