package com.example.lengua.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lengua.network.Evaluation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationsScreen(
    viewModel: EvaluationsViewModel = viewModel(factory = EvaluationsViewModelFactory(LocalContext.current)),
    onMenuClick: () -> Unit // ✅ PARÁMETRO AÑADIDO
) {
    val uiState by viewModel.uiState.collectAsState()

    // ✅ SCAFFOLD Y TOPAPPBAR RESTAURADOS
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluaciones") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.error}", color = Color.Red)
                    }
                }
                uiState.evaluations.isEmpty() -> {
                    EmptyEvaluationsState()
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(uiState.evaluations) { evaluation ->
                            EvaluationCard(evaluation)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyEvaluationsState() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = "No hay evaluaciones",
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tienes evaluaciones asignadas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cuando un profesor te asigne una, aparecerá aquí.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EvaluationCard(evaluation: Evaluation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(evaluation.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            evaluation.descripcion?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Tipo: ${evaluation.tipo}", fontSize = 14.sp)
                Text("Profesor: ${evaluation.profesor}", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Fecha límite: ${evaluation.fechaLimite}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            
            val statusText = "Estado: ${evaluation.estadoEstudiante}"
            val statusColor = if (evaluation.estadoEstudiante.equals("entregada", ignoreCase = true)) {
                Color(0xFF4CAF50) // Verde
            } else {
                Color.Red
            }
            
            Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
        }
    }
}
