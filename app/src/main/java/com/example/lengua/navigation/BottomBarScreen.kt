package com.example.lengua.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.vector.ImageVector

// REVERTIDO: Define los elementos correctos de la barra de navegación
sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )

    object Evaluations : BottomBarScreen(
        route = "evaluations",
        title = "Evaluations",
        icon = Icons.Default.Book
    )

    object Security : BottomBarScreen(
        route = "security",
        title = "Security",
        icon = Icons.Default.Security
    )

    object Progress : BottomBarScreen(
        route = "progress",
        title = "Progress",
        icon = Icons.Default.Dashboard
    )
}
