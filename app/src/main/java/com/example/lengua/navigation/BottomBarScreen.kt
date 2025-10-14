package com.example.lengua.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector

// Objeto sellado que define cada pantalla en la barra de navegación inferior
sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // Pantalla de Inicio
    object Home : BottomBarScreen(
        route = "home_dashboard",
        title = "Home",
        icon = Icons.Default.Home
    )

    // Pantalla de Clases
    object Classes : BottomBarScreen(
        route = "classes",
        title = "Classes",
        icon = Icons.AutoMirrored.Filled.MenuBook
    )

    // Pantalla de "Security" (marcador de posición)
    object Security : BottomBarScreen(
        route = "security",
        title = "Security",
        icon = Icons.Default.Security
    )

    // Pantalla de "Fitness" (marcador de posición)
    object Fitness : BottomBarScreen(
        route = "fitness",
        title = "Fitness",
        icon = Icons.Default.FitnessCenter
    )
}
