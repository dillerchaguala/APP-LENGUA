
package com.example.lengua.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lengua.R

data class Plan(
    val name: String,
    val tag: String,
    val price: String,
    val description: String,
    val features: List<String>,
    val color: Color,
    val priceColor: Color
)

val plans = listOf(
    Plan(
        name = "Plan basico",
        tag = "Basico",
        price = "$180000.00",
        description = "Perfecto para comezar tu aprendizaje de ingles desde cero",
        features = listOf(
            "Acceso a 20 clases basicas",
            "Material de estudio digital",
            "Evaluaciones semanales"
        ),
        color = Color(0xFF42A5F5),
        priceColor = Color(0xFF42A5F5)
    ),
    Plan(
        name = "Plan intermedio",
        tag = "Basico",
        price = "$280000.00",
        description = "Perfecto para comezar tu aprendizaje de ingles desde cero",
        features = listOf(
            "Acceso a 20 clases basicas",
            "Material de estudio digital",
            "Evaluaciones semanales"
        ),
        color = Color(0xFF66BB6A),
        priceColor = Color(0xFF66BB6A)
    )
)

@Composable
fun PlansAndPricingScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PlansAndPricingHeader()
            }
            items(plans) { plan ->
                PlanCard(plan = plan)
            }
        }
    }
}

@Composable
fun PlansAndPricingHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF00ACC1))
            .padding(24.dp)
    ) {
        Text(
            text = "Gestión de planes y precios",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Administra los planes disponibles y sus precios para los estudiantes.",
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
fun PlanCard(plan: Plan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(plan.color)
            )
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with actual icon
                        contentDescription = "Plan icon",
                        modifier = Modifier.size(40.dp),
                        tint = plan.color
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = plan.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(plan.color.copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(text = plan.tag, color = plan.color, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = plan.price,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = plan.priceColor
                    )
                    Text(
                        text = "/mes",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)

                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = plan.description, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Caracteristicas incluidas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                plan.features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Feature",
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = feature, fontSize = 14.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlansAndPricingScreenPreview() {
    PlansAndPricingScreen()
}
