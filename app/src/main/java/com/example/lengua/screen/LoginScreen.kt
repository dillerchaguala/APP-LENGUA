package com.example.lengua.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lengua.R
import com.example.lengua.data.repository.Result

// --- Pantalla de Login ---
@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(LocalContext.current))
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // State for password visibility
    val uiState by loginViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.loginResult) {
        uiState.loginResult?.let {
            when (it) {
                is Result.Success -> {
                    Toast.makeText(context, "Login exitoso", Toast.LENGTH_SHORT).show()
                    
                    val userRole = it.data.role

                    val destination = when (userRole) {
                        "admin" -> "admin_dashboard_screen"
                        "profesor" -> "teacher_dashboard_screen"
                        else -> "home_screen"
                    }
                    navController.navigate(destination) {
                        popUpTo("main_screen") { inclusive = true }
                    }
                }
                is Result.Error -> {
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val gradient = Brush.verticalGradient(colors = listOf(Color(0xFF00A99D), Color(0xFF004D40)))

    Column(
        modifier = Modifier.fillMaxSize().background(gradient).padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo La Lengua", modifier = Modifier.height(110.dp))

        Spacer(modifier = Modifier.height(32.dp))

        Text("Iniciar sesión", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)

        Spacer(modifier = Modifier.height(24.dp))

        // --- Formulario ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Usuario", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            TextField(
                value = username, onValueChange = { username = it }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0x33FFFFFF),
                    focusedContainerColor = Color(0x33FFFFFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White, // <-- FIXED
                    cursorColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Contraseña", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            TextField(
                value = password, onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(50),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                    IconButton(onClick = { passwordVisible = !passwordVisible }){
                        Icon(imageVector  = image, description, tint = Color.White.copy(alpha = 0.7f))
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0x33FFFFFF),
                    focusedContainerColor = Color(0x33FFFFFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White, // <-- FIXED
                    cursorColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { /* TODO: Navegar a pantalla de registro */ }) {
            Text("¿No tienes cuenta? crea una", color = Color.White, textDecoration = TextDecoration.Underline)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Button(
                onClick = { loginViewModel.login(username, password) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Iniciar sesión", color = Color(0xFF004D40), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SocialLoginButton("Continuar con Google", onClick = { /* TODO */ }, icon = R.drawable.google)
            Spacer(modifier = Modifier.height(16.dp))
            SocialLoginButton("Continuar con Apple", onClick = { /* TODO */ }, icon = R.drawable.apple)
            Spacer(modifier = Modifier.height(16.dp))
            SocialLoginButton("Continuar con Facebook", onClick = { /* TODO */ }, icon = R.drawable.facebook)
        }
    }
}

@Composable
private fun SocialLoginButton(text: String, onClick: () -> Unit, icon: Int) {
    Button(
        onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40), contentColor = Color.White),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Image(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(24.dp))
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}
