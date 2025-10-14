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

    val items = listOf(
        "scheduled_classes" to Icons.Default.Book,
        "evaluations" to Icons.Default.Assessment,
        "club" to Icons.Default.Group
    )
    var selectedItem by remember { mutableStateOf(items[0]) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.second, contentDescription = null) },
                        label = { Text(item.first.replaceFirstChar { it.uppercase().replace("_", " ") }) },
                        selected = item == selectedItem,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedItem = item
                            navController.navigate(item.first) { launchSingleTop = true }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) { 
        NavHost(navController = navController, startDestination = "scheduled_classes") {
            composable("scheduled_classes") {
                ScheduledClassesScreen(onMenuClick = { 
                    scope.launch { drawerState.open() } 
                })
            }
            composable("evaluations") {
                Scaffold(
                    topBar = { 
                        TopAppBar(
                            title = { Text("Evaluaciones") },
                            navigationIcon = { 
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)){
                        EvaluationsScreen()
                    }
                }
            }
            composable("club") {
                Scaffold(
                    topBar = { 
                        TopAppBar(
                            title = { Text("Club") },
                            navigationIcon = { 
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)){
                        ClubScreen()
                    }
                }
            }
        }
    }
}
