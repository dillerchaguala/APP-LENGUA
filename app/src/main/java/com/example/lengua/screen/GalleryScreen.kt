
package com.example.lengua.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lengua.R

@Composable
fun GalleryScreen() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Handle FAB click */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Header()
            }
            item {
                GalleryStats()
            }
            item {
                GalleryItem(
                    title = "Phrasal Verbs Más Comunes",
                    description = "Colección visual de los phrasal verbs más utilizados en inglés cotidiano con ejemplos contextualizados.",
                    author = "Por Admin",
                    date = "8/9/2025",
                    imageRes = R.drawable.ic_launcher_background, // Replace with actual image
                    tag = "Infografias"
                )
            }
            item {
                GalleryItem(
                    title = "Título del Elemento",
                    description = "Descripción del elemento.",
                    author = "Autor",
                    date = "Fecha",
                    imageRes = R.drawable.ic_launcher_foreground, // Replace with actual image
                    tag = "Fotos"
                )
            }
        }
    }
}

@Composable
fun Header() {
    Text(
        text = "Gestión de Galería",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun GalleryStats() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatCard(count = "3", label = "Imágenes", color = Color(0xFFE0F7FA))
        StatCard(count = "2", label = "Videos", color = Color(0xFFF3E5F5))
        StatCard(count = "5", label = "Total", color = Color(0xFFE8F5E9))
    }
}

@Composable
fun StatCard(count: String, label: String, color: Color) {
    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = label, fontSize = 14.sp)
        }
    }
}

@Composable
fun GalleryItem(
    title: String,
    description: String,
    author: String,
    date: String,
    imageRes: Int,
    tag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = tag,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { /* Handle more options */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                }
                Text(text = description, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = author, fontSize = 12.sp, color = Color.Gray)
                    Text(text = date, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GalleryScreenPreview() {
    GalleryScreen()
}
