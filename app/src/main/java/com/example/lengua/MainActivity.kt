package com.example.lengua

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lengua.screen.AdminDashboardScreen
import com.example.lengua.screen.HomeScreen
import com.example.lengua.screen.LoginScreen
import com.example.lengua.screen.ProfileScreen
import com.example.lengua.screen.SplashScreen
import com.example.lengua.ui.theme.LenguaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LenguaTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash_screen") {
                    composable("splash_screen") {
                        SplashScreen(navController = navController)
                    }
                    composable("main_screen") {
                        LoginScreen(navController = navController)
                    }
                    composable("home_screen") {
                        HomeScreen(mainNavController = navController)
                    }
                    composable("profile_screen") {
                        ProfileScreen(navController = navController)
                    }
                    composable("admin_dashboard_screen") {
                        AdminDashboardScreen(
                            onLogout = {
                                navController.navigate("main_screen") {
                                    popUpTo("splash_screen") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("teacher_dashboard_screen") {
                        // TODO: Crear la pantalla para el profesor
                        PlaceholderScreen(name = "Profesor Dashboard")
                    }
                }
            }
        }
    }
}

// Composable temporal para evitar crashes
@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Pantalla: $name")
    }
}
