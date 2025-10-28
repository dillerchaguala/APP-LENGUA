package com.example.lengua.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lengua.data.repository.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(viewModel: CreateUserViewModel, onUserCreated: () -> Unit) {
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var emailLogin by remember { mutableStateOf("") }
    var emailPersonal by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("student") }
    
    // State for dynamic dropdowns
    val bloquesState by viewModel.bloquesState.collectAsState()
    val especializacionesState by viewModel.especializacionesState.collectAsState()
    var bloqueSeleccionado by remember { mutableStateOf<String?>(null) }
    var especializacionSeleccionada by remember { mutableStateOf<String?>(null) }

    val createUserState by viewModel.createUserState.collectAsState()
    val context = LocalContext.current

    // Load data on init
    LaunchedEffect(Unit) {
        viewModel.loadBloques()
        viewModel.loadEspecializaciones()
    }

    // Effect to handle creation result
    LaunchedEffect(createUserState) {
        when (val result = createUserState) {
            is Result.Success -> {
                Toast.makeText(context, "Usuario creado con éxito", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateUserState() 
                onUserCreated() // Navigate back
            }
            is Result.Error -> {
                Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                viewModel.resetCreateUserState()
            }
            is Result.Loading -> {}
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Nuevo Usuario", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            FormTextField(label = "Nombres *", value = nombres, onValueChange = { nombres = it })
            Spacer(modifier = Modifier.height(16.dp))

            FormTextField(label = "Apellidos *", value = apellidos, onValueChange = { apellidos = it })
            Spacer(modifier = Modifier.height(16.dp))

            FormTextField(label = "Correo Electrónico (Login) *", value = emailLogin, onValueChange = { emailLogin = it })
            Spacer(modifier = Modifier.height(16.dp))

            FormTextField(label = "Correo Personal (Recuperación)", value = emailPersonal, onValueChange = { emailPersonal = it })
            Spacer(modifier = Modifier.height(16.dp))

            FormDropdown(label = "Rol *", items = listOf("student", "profesor", "admin"), selectedValue = selectedRole, onItemSelected = { selectedRole = it })
            Spacer(modifier = Modifier.height(16.dp))

            // --- Dynamic Bloques Dropdown ---
            Text("Bloque Asignado", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
            when (val state = bloquesState) {
                is Result.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                is Result.Success -> {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = bloqueSeleccionado ?: "Sin asignar",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Sin asignar") }, onClick = {
                                bloqueSeleccionado = null
                                expanded = false
                            })
                            state.data.forEach { bloque ->
                                DropdownMenuItem(text = { Text(bloque.getDisplayName()) }, onClick = {
                                    bloqueSeleccionado = bloque.getDisplayName()
                                    expanded = false
                                })
                            }
                        }
                    }
                }
                is Result.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Dynamic Especializaciones Dropdown ---
            Text("Especialización", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
            when (val state = especializacionesState) {
                is Result.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                is Result.Success -> {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = especializacionSeleccionada ?: "Sin especialización",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Sin especialización") }, onClick = {
                                especializacionSeleccionada = null
                                expanded = false
                            })
                            state.data.forEach { esp ->
                                DropdownMenuItem(text = { Text(esp.nombre) }, onClick = {
                                    especializacionSeleccionada = esp.nombre
                                    expanded = false
                                })
                            }
                        }
                    }
                }
                is Result.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Password Field ---
            Column(Modifier.fillMaxWidth()) {
                Text("Contraseña *", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Ocultar" else "Mostrar")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            val isLoading = createUserState is Result.Loading
            val canSubmit = nombres.isNotBlank() && apellidos.isNotBlank() && emailLogin.isNotBlank() && password.isNotBlank()

            // --- Action Buttons ---
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        viewModel.createUser(
                            username = emailLogin.substringBefore('@'),
                            email = emailLogin,
                            password = password,
                            firstName = nombres,
                            lastName = apellidos,
                            role = selectedRole,
                            bloqueAsignado = bloqueSeleccionado,
                            especializacion = especializacionSeleccionada,
                            correoPersonal = emailPersonal.takeIf { it.isNotBlank() }
                        )
                    }, 
                    modifier = Modifier.weight(1f),
                    enabled = canSubmit && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("CREAR USUARIO")
                    }
                }
                Button(
                    onClick = onUserCreated, // Go back
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    enabled = !isLoading
                ) {
                    Text("CANCELAR")
                }
            }
        }
    }
}

@Composable
fun FormTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDropdown(label: String, items: List<String>, selectedValue: String, onItemSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                items.forEach { item ->
                    DropdownMenuItem(text = { Text(item) }, onClick = {
                        onItemSelected(item)
                        expanded = false
                    })
                }
            }
        }
    }
}
