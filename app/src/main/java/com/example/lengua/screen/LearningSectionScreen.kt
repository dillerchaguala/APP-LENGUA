package com.example.lengua.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningSectionScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // ✅ Items para el NUEVO menú de hamburguesa específico de esta sección
    val items = listOf(
        "Clases Programadas" to Icons.Default.Book,
        "Evaluaciones" to Icons.Default.Assessment,
        "Club" to Icons.Default.Group
    )
    // Rutas correspondientes a los items
    val routes = listOf("scheduled_classes", "evaluations", "club")

    var selectedItemIndex by remember { mutableStateOf(0) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) },
                        selected = index == selectedItemIndex,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedItemIndex = index
                            navController.navigate(routes[index]) { 
                                // Limpia el backstack para no acumular pantallas
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) { 
        // ✅ NavHost interno para gestionar la navegación de esta sección
        NavHost(navController = navController, startDestination = routes[0]) {
            composable(routes[0]) {
                // Pasamos la acción para abrir ESTE menú, no el principal
                ScheduledClassesScreen(onMenuClick = { 
                    scope.launch { drawerState.open() } 
                })
            }
            composable(routes[1]) {
                EvaluationsScreen(onMenuClick = { 
                    scope.launch { drawerState.open() } 
                })
            }
            composable(routes[2]) {
                ClubScreen(onMenuClick = { 
                    scope.launch { drawerState.open() } 
                })
            }
        }
    }
}
