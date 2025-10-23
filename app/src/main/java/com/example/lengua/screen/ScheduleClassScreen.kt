package com.example.lengua.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lengua.network.Professor
import com.example.lengua.network.Student
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleClassScreen() {
    val viewModel: ScheduleClassViewModel = viewModel(factory = ScheduleClassViewModelFactory(LocalContext.current))

    val professors by viewModel.professors.collectAsState()
    val students by viewModel.students.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    var className by remember { mutableStateOf("") }
    var selectedProfessor by remember { mutableStateOf<Professor?>(null) }
    var classDate by remember { mutableStateOf("") } // YYYY-MM-DD
    var classTime by remember { mutableStateOf("") } // HH:mm
    var meetLink by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedStudentIds by remember { mutableStateOf(emptySet<Int>()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Date Picker Dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            classDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Time Picker Dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            classTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true // 24-hour format
    )

    LaunchedEffect(success) {
        if (success) {
            // TODO: Navigate back or show success message
        }
    }
    LaunchedEffect(error) {
        error?.let {
            // TODO: Show error Snackbar
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Nueva Clase") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Nombre de la Clase * ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                ProfessorDropdown(professors, selectedProfessor) { selectedProfessor = it }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker TextField
                OutlinedTextField(
                    value = classDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de la Clase * ") },
                    placeholder = { Text("YYYY-MM-DD") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha",
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Time Picker TextField
                OutlinedTextField(
                    value = classTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hora de la Clase * ") },
                    placeholder = { Text("HH:mm") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Seleccionar hora",
                            modifier = Modifier.clickable { timePickerDialog.show() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = meetLink,
                        onValueChange = { meetLink = it },
                        label = { Text("Enlace de Google Meet") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { /* TODO: Generate Meet link */ }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Generar Meet")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                StudentSelectionSection(students, selectedStudentIds) { selectedStudentIds = it }

                Spacer(modifier = Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { /* TODO: Cancel */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("CANCELAR")
                    }
                    Button(
                        onClick = {
                            selectedProfessor?.let {
                                viewModel.createClass(
                                    nombre = className,
                                    fecha = classDate,
                                    hora = classTime,
                                    descripcion = description,
                                    meetLink = meetLink,
                                    profesor = it.fullName,
                                    estudiantesIds = selectedStudentIds.toList()
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = classDate.isNotBlank() && classTime.isNotBlank() && className.isNotBlank() && selectedProfessor != null
                    ) {
                        Text("AGREGAR CLASE")
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessorDropdown(professors: List<Professor>, selectedProfessor: Professor?, onProfessorSelected: (Professor) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            value = selectedProfessor?.fullName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Nombre del Profesor * ") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            professors.forEach { professor ->
                DropdownMenuItem(
                    text = { Text(professor.fullName) },
                    onClick = {
                        onProfessorSelected(professor)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSelectionSection(students: List<Student>, selectedIds: Set<Int>, onSelectionChanged: (Set<Int>) -> Unit) {
    var selectedLevel by remember { mutableStateOf("Todos los niveles") }
    val levels = listOf("Todos los niveles", "Nivel A1", "Nivel A2", "Nivel B1")

    Text("Selecciona Estudiantes *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                value = selectedLevel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                levels.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level) },
                        onClick = { 
                            selectedLevel = level
                            expanded = false
                        }
                    )
                }
            }
        }

        Button(onClick = {
            if (selectedIds.size == students.size) {
                onSelectionChanged(emptySet())
            } else {
                onSelectionChanged(students.map { it.id }.toSet())
            }
        }) {
            Text("Seleccionar todos")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
    ) {
        LazyColumn {
            items(students) { student ->
                StudentListItem(
                    student = student,
                    isSelected = student.id in selectedIds,
                    onCheckedChange = {
                        val newSelection = if (student.id in selectedIds) {
                            selectedIds - student.id
                        } else {
                            selectedIds + student.id
                        }
                        onSelectionChanged(newSelection)
                    }
                )
            }
        }
    }
}

@Composable
fun StudentListItem(student: Student, isSelected: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(student.fullName, fontWeight = FontWeight.Bold)
            Text(student.email, color = Color.Gray, fontSize = 14.sp)
        }
    }
    HorizontalDivider()
}

@Preview(showBackground = true)
@Composable
fun ScheduleClassScreenPreview() {
    ScheduleClassScreen()
}
