package com.example.lengua.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lengua.screen.HomeDashboardScreen
import com.example.lengua.screen.LearningSectionScreen
import com.example.lengua.screen.UserState

@Composable
fun BottomNavGraph(
    bottomNavController: NavHostController,
    mainNavController: NavController, // El controlador de navegación principal de la app
    userState: UserState,
    onLogout: () -> Unit
) {
    NavHost(
        navController = bottomNavController,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(route = BottomBarScreen.Home.route) {
            HomeDashboardScreen(
                userState = userState,
                navController = mainNavController, // Usa el controlador principal para acciones como ir a Perfil
                onLogout = onLogout
            )
        }
        // ✅ RUTA ACTUALIZADA
        composable(route = BottomBarScreen.Classes.route) {
            LearningSectionScreen()
        }
        composable(route = BottomBarScreen.Security.route) {
            // Reusamos la pantalla de sección de aprendizaje como marcador de posición
            LearningSectionScreen()
        }
        composable(route = BottomBarScreen.Fitness.route) {
            // Reusamos la pantalla de sección de aprendizaje como marcador de posición
            LearningSectionScreen()
        }
    }
}
