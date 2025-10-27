
package com.example.lengua.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lengua.network.Bloque
import kotlinx.coroutines.launch

// --- MODELO Y LISTA PARA EL MENÚ ---
data class AdminMenuItem(val route: String, val title: String, val icon: ImageVector)

val adminMenuItems = listOf(
    AdminMenuItem("admin_dashboard", "Dashboard", Icons.Default.Dashboard),
    AdminMenuItem("manage_users", "Gestionar usuarios", Icons.Default.People),
    AdminMenuItem("schedule_classes", "Programar clases", Icons.Default.CalendarToday),
    AdminMenuItem("manage_students", "Gestión de estudiantes", Icons.Default.School),
    AdminMenuItem("blocks", "Bloques", Icons.Default.ViewModule),
    AdminMenuItem("gallery_management", "Gestión de galeria", Icons.Default.PhotoLibrary),
    AdminMenuItem("specializations", "Especializaciones", Icons.Default.Star),
    AdminMenuItem("plans_pricing", "Planes y precios", Icons.Default.AttachMoney),
    AdminMenuItem("sales_log", "Registro de ventas", Icons.Default.Receipt),
    AdminMenuItem("subscription_management", "Gestión de suscripciones", Icons.Default.CardMembership)
)

// --- PANTALLA PRINCIPAL DEL DASHBOARD ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var currentTitle by remember { mutableStateOf(adminMenuItems.first().title) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { 
            DrawerContent(currentTitle, adminMenuItems, onLogout) { route, title ->
                scope.launch { drawerState.close() }
                navController.navigate(route) {
                    launchSingleTop = true
                    popUpTo(navController.graph.startDestinationId)
                }
                currentTitle = title
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentTitle) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavHost(navController = navController, startDestination = adminMenuItems.first().route) {
                    composable("blocks") { AdminBlocksScreen() }
                    composable("manage_users") { AdminUsersScreen() }
                    composable("schedule_classes") { ScheduleClassScreen() }
                    composable("gallery_management") { GalleryScreen() }
                    composable("plans_pricing") { PlansAndPricingScreen() }
                    composable("sales_log") { SalesRecordScreen() }
                    composable("specializations") { SpecializationsScreen() } // <-- CORREGIDO
                    
                    val handledRoutes = setOf("blocks", "manage_users", "schedule_classes", "gallery_management", "plans_pricing", "sales_log", "specializations")
                    adminMenuItems.filter { it.route !in handledRoutes }.forEach { item ->
                        composable(item.route) { AdminPlaceholderScreen(title = item.title) }
                    }
                }
            }
        }
    }
}

// --- CONTENIDO DEL MENÚ LATERAL ---
@Composable
fun DrawerContent(currentTitle: String, items: List<AdminMenuItem>, onLogout: () -> Unit, onItemClick: (String, String) -> Unit) {
    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            items.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title) },
                    selected = item.title == currentTitle,
                    onClick = { onItemClick(item.route, item.title) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión") },
            label = { Text("Cerrar sesión") },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(Modifier.height(12.dp))
    }
}

// --- SECCIÓN DE BLOQUES ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBlocksScreen(viewModel: AdminBlocksViewModel = viewModel(factory = AdminBlocksViewModelFactory(LocalContext.current))) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedBlock by viewModel.selectedBlock.collectAsState()

    selectedBlock?.let {
        BlockDetailDialog(
            block = it,
            onDismiss = { viewModel.onDialogDismiss() },
            onEdit = { /* TODO */ },
            onDelete = { /* TODO */ }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Crear bloque") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Crear bloque") },
                onClick = { /* TODO: viewModel.createBlock() */ }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                uiState.error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${uiState.error}", color = Color.Red) } // ✅ CORREGIDO
                else -> {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        uiState.blocksByLevel.forEach { (level, blocks) ->
                            item { Text(level, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 8.dp)) }
                            items(blocks.chunked(2)) { rowBlocks ->
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    rowBlocks.forEach { block ->
                                        Box(modifier = Modifier.weight(1f)) { BlockCard(block, onClick = { viewModel.onBlockSelected(block) }) }
                                    }
                                    if (rowBlocks.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlockCard(block: Bloque, onClick: () -> Unit) {
    val color = try { Color(android.graphics.Color.parseColor(block.grupoColor)) } catch (e: Exception) { Color.Gray }
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.height(16.dp))
            Text(block.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(block.estado, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun BlockDetailDialog(block: Bloque, onDismiss: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Detalles de ${block.nivel} ${block.nombre}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Profesores asignados", fontWeight = FontWeight.Bold); Text("• luisa flores"); Text("• Profesor Test"); Spacer(modifier = Modifier.height(16.dp))
                Text("Clases, horarios y enlaces de Meet", fontWeight = FontWeight.Bold); Text("Inglés 2 - Martes 9:00 - 11:00"); Text("Inglés 3 - Miércoles 12:00 - 2:00"); Spacer(modifier = Modifier.height(16.dp))
                Text("Misiones", fontWeight = FontWeight.Bold); Text("• conversasion"); Text("• descripción"); Text("• pronombres"); Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Editar Bloque") }
                    Button(onClick = onDelete, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Eliminar") }
                }
            }
        }
    }
}


// --- SECCIÓN DE GESTIÓN DE USUARIOS (CON ESTILO CORREGIDO) ---
data class DisplayUser(val id: Int, val nombre: String, val correo: String, val rol: String, val bloque: String, val especializacion: String, val estado: String)

// ✅ CORREGIDO: Se elimina el salto de línea '\n'
val mockUsers = List(10) { 
    DisplayUser(1, "Juan andres rodriguez", "Juanandres@gmail.com", "Student", "Sin asignar", "Sin asignar", "Activo") 
}

@Composable
fun AdminUsersScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
            .padding(16.dp)
    ) {
        Button(
            onClick = { /* TODO */ },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar usuario", modifier = Modifier.padding(vertical = 4.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Text(
                    text = "Lista de usuarios", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn {
                    item { UserTableHeader() }
                    items(mockUsers) { user -> UserTableRow(user) }
                }
            }
        }
    }
}

@Composable
fun UserTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF3F51B5))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ CORREGIDO: Se ajustan los pesos para dar más espacio
        Text("ID", color = Color.White, modifier = Modifier.weight(0.4f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text("Nombre", color = Color.White, modifier = Modifier.weight(1.3f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text("Correo", color = Color.White, modifier = Modifier.weight(1.3f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text("Rol", color = Color.White, modifier = Modifier.weight(0.8f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text("Bloque", color = Color.White, modifier = Modifier.weight(1.2f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center)
        Text("Especialización", color = Color.White, modifier = Modifier.weight(1.2f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center)
        Text("Estado", color = Color.White, modifier = Modifier.weight(0.8f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
    HorizontalDivider()
}

@Composable
fun UserTableRow(user: DisplayUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ CORREGIDO: Se ajustan los pesos para coincidir con la cabecera
        Text(user.id.toString(), modifier = Modifier.weight(0.4f), fontSize = 14.sp, color = Color.DarkGray)
        Text(user.nombre, modifier = Modifier.weight(1.3f), fontSize = 14.sp, color = Color.DarkGray)
        Text(user.correo, modifier = Modifier.weight(1.3f), fontSize = 14.sp, color = Color.DarkGray)
        Text(user.rol, modifier = Modifier.weight(0.8f), fontSize = 14.sp, color = Color.DarkGray)
        Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) { StatusPill(user.bloque, Color(0xFF26C6DA), Color.White) }
        Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) { StatusPill(user.especializacion, Color(0xFF673AB7), Color.White) }
        Box(modifier = Modifier.weight(0.8f), contentAlignment = Alignment.Center) { StatusPill(user.estado, Color(0xFF66BB6A), Color.White) }
    }
    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
}

@Composable
fun StatusPill(text: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            // ✅ CORREGIDO: Padding horizontal para píldora ancha
            .padding(horizontal = 12.dp, vertical = 6.dp), 
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
    }
}


// --- PANTALLA DE MARCADOR DE POSICIÓN ---
@Composable
fun AdminPlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Pantalla de $title", style = MaterialTheme.typography.headlineMedium)
    }
}
