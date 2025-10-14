package com.example.lengua

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lengua.screen.HomeScreen
import com.example.lengua.screen.LoginScreen
import com.example.lengua.screen.ProfileScreen // <-- IMPORTACIÓN AÑADIDA
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
                        HomeScreen(navController = navController)
                    }
                    // RUTA NUEVA AÑADIDA
                    composable("profile_screen") {
                        ProfileScreen(navController = navController)
                    }
                }
            }
        }
    }
}
