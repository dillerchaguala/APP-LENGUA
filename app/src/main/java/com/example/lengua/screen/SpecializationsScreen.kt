package com.example.lengua.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Specialization(
    val id: String,
    val name: String,
    val description: String,
    val duration: String,
    val price: String,
    val createdDate: String,
    val isActive: Boolean
)

val mockSpecializations = listOf(
    Specialization("1", "Ingles tecnico", "Terminología especializada para profesionales de IT, ingeniería y ciencias", "12 semanas", "$399.00", "9/09/2025", true),
    Specialization("2", "Ingles para call center", "Enfocado en la comunicación fluida y efectiva para servicio al cliente", "8 semanas", "$249.00", "15/08/2025", true),
    Specialization("3", "Ingles de negocios", "Desarrolla habilidades para negociaciones, presentaciones y marketing", "16 semanas", "$499.00", "21/07/2025", false)
)

@Composable
fun SpecializationsScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF0F2F5)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for the FAB if it were inside the list
        ) {
            item {
                SpecializationsHeader()
            }
            items(mockSpecializations) { specialization ->
                SpecializationCard(specialization = specialization, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }
}

@Composable
fun SpecializationsHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
         Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF00BFFF), Color(0xFF00B2B2))
                    )
                )
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Gestión de especializaciones", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Administra las especializaciones disponibles para los estudiantes.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
            }
        }
        Button(
            onClick = { /* TODO: Navigate to create specialization screen */ },
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nueva especialización", color = Color.White)
        }
    }
}

@Composable
fun SpecializationCard(specialization: Specialization, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(specialization.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                StatusPill(isActive = specialization.isActive)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(specialization.description, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoRow("Duración:", specialization.duration)
            InfoRow("Precio:", specialization.price)
            InfoRow("Creada:", specialization.createdDate)

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))) { Text("Editar") }
                Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))) { Text("Desactivar") }
            }
            Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))) { Text("Eliminar") }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(100.dp))
        Text(value)
    }
}

@Composable
fun StatusPill(isActive: Boolean) {
    val (text, color) = if (isActive) "Activa" to Color(0xFF4CAF50) else "Inactiva" to Color.Gray
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun SpecializationsScreenPreview() {
    SpecializationsScreen()
}
