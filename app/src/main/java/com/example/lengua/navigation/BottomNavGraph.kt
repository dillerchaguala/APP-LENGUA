package com.example.lengua.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lengua.screen.HomeDashboardScreen
import com.example.lengua.screen.LearningSectionScreen
import com.example.lengua.screen.UserState

// VERSIÓN FINAL RESTAURADA
@Composable
fun BottomNavGraph(
    bottomNavController: NavHostController,
    mainNavController: NavController, 
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
                navController = mainNavController
            )
        }
        composable(route = BottomBarScreen.Evaluations.route) {
            LearningSectionScreen()
        }
        composable(route = BottomBarScreen.Security.route) {
            PlaceholderScreen(title = "Security")
        }
        composable(route = BottomBarScreen.Progress.route) {
            PlaceholderScreen(title = "Progress")
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Pantalla de $title")
    }
}
