package com.example.lengua.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun HomeDashboardScreen(userState: UserState, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { WelcomeBanner(name = userState.firstName) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { AvatarCard() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { ProgressCard() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { DailyChallengeCard() }

        // --- ✅ SECCIÓN DE MISIONES AÑADIDA ---
        item { Spacer(modifier = Modifier.height(32.dp)) }
        item {
            Text(
                text = "MISIONES",
                color = Color.Black,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { 
            MissionCard(
                color1 = Color(0xFF6A1B9A), 
                color2 = Color(0xFF42A5F5),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Gramática pasado",
                subtitle = "Quiz interactivo",
                points = 1250,
                starColor = Color(0xFFFFD600)
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { 
            MissionCard(
                color1 = Color(0xFFEF6C00), 
                color2 = Color(0xFF66BB6A),
                icon = Icons.Default.Edit,
                title = "Gramática pasado",
                subtitle = "Quiz interactivo",
                points = 1250,
                starColor = Color(0xFFFFD600)
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { 
            MissionCard(
                color1 = Color(0xFFFFA726), 
                color2 = Color(0xFF26C6DA),
                icon = Icons.Default.Cloud,
                title = "Ir de Compras",
                subtitle = "Jugar en tiempo real",
                points = 1250,
                starColor = Color(0xFFFFD600)
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// --- FUNCIONES DE LAS TARJETAS ---
@Composable
fun WelcomeBanner(name: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Button(onClick = { }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C896))) {
            Text("Welcome ${if (name.isNotEmpty()) name else ""}", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AvatarCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF4A00E0))) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tu avatar", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Personaliza tu avatar para acompañar a Lingo en su viaje migratorio.", color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { }, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f))) { Text("Personalizar", color = Color.White) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, "Avatar", tint = Color.Cyan, modifier = Modifier.size(60.dp))
            }
        }
    }
}

@Composable
fun ProgressCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF00C896))) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tu progreso", color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nivel actual: principiante", color = Color.Black.copy(alpha = 0.8f))
            }
            Icon(Icons.Default.Assessment, "Graph icon", tint = Color.Black)
        }
    }
}

@Composable
fun DailyChallengeCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF59D))) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Reto diario", color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Whatshot, "Fire icon", tint = Color(0xFFFF5722), modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {}, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD180))) {
                Text("¡En racha!", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dia 0 de racha", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 18.sp)
            Text("Completa el reto de hoy para mantener tu racha invicta", color = Color.Black.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                repeat(5) { i ->
                    Box(modifier = Modifier.padding(horizontal = 4.dp).size(40.dp, 8.dp).clip(RoundedCornerShape(4.dp)).background(Color.Gray.copy(alpha = 0.3f)))
                }
            }
        }
    }
}

// --- ✅ NUEVA FUNCIÓN PARA LAS TARJETAS DE MISIÓN ---
@Composable
fun MissionCard(
    color1: Color,
    color2: Color,
    icon: ImageVector,
    title: String,
    subtitle: String,
    points: Int,
    starColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier.background(Brush.verticalGradient(listOf(color1, color2)))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Completa esta misión para avanzar en tu aprendizaje", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(points.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Star, contentDescription = "Star", tint = starColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.3f))
                ) {
                    Text("Jugar ahora", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
