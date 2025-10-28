package com.example.lengua.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lengua.data.repository.Result
import com.example.lengua.ui.theme.LenguaTheme


// Helper function to get file name from URI
fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = it.getString(nameIndex)
            }
        }
    }
    return fileName
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGalleryItemScreen(
    onCancel: () -> Unit,
    viewModel: GalleryViewModel = viewModel(factory = GalleryViewModelFactory(LocalContext.current))
) {
    var contentType by remember { mutableStateOf("Imagen") } // Default to Image
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fotos") }
    var contentSource by remember { mutableStateOf("URL") } // URL or File
    var contentUrl by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("Admin") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }

    val createMediaState by viewModel.createMediaState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(createMediaState) {
        when (val result = createMediaState) {
            is Result.Success -> {
                Toast.makeText(context, "Contenido agregado con éxito", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateMediaState()
                onCancel() // Navigate back
            }
            is Result.Error -> {
                Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                viewModel.resetCreateMediaState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Agregar Contenido a la Galería") })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            FormDropdown(
                label = "Tipo de contenido *",
                items = listOf("Imagen", "Video"),
                selectedValue = contentType,
                onItemSelected = { contentType = it }
            )

            FormTextField(label = "Título *", value = title, onValueChange = { title = it })

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            FormDropdown(
                label = "Categoría *",
                items = listOf("Videos", "Infografías", "Fotos"),
                selectedValue = category,
                onItemSelected = { category = it }
            )

            Column {
                Text("Contenido:", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.selectableGroup()) {
                    RadioButtonWithText(text = "Usar URL", selected = contentSource == "URL") { contentSource = "URL" }
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButtonWithText(text = "Subir archivo", selected = contentSource == "File") { contentSource = "File" }
                }
            }

            if (contentSource == "URL") {
                FormTextField(label = "URL del contenido", value = contentUrl, onValueChange = { contentUrl = it })
                LaunchedEffect(Unit) { selectedFileUri = null }
            } else {
                val pickerType = if (contentType == "Imagen") "image/*" else "video/*"
                FileSelector(selectedFileUri, onFileSelect = { filePickerLauncher.launch(pickerType) })
                LaunchedEffect(Unit) { contentUrl = "" }
            }

            FormTextField(label = "URL del thumbnail (opcional)", value = thumbnailUrl, onValueChange = { thumbnailUrl = it })

            FormTextField(label = "Autor *", value = author, onValueChange = { author = it })

            val isLoading = createMediaState is Result.Loading
            val canSubmit = title.isNotBlank() && ((contentSource == "URL" && contentUrl.isNotBlank()) || (contentSource == "File" && selectedFileUri != null))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !isLoading) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val backendType = when (contentType) {
                            "Video" -> "video"
                            "Imagen" -> "image"
                            else -> contentType.lowercase()
                        }

                        if (contentSource == "File") {
                            selectedFileUri?.let {
                                viewModel.createMediaItemWithFile(
                                    type = backendType,
                                    title = title,
                                    description = description,
                                    category = category,
                                    author = author,
                                    fileUri = it
                                )
                            }
                        } else {
                            viewModel.createMediaItem(
                                type = backendType,
                                title = title,
                                description = description,
                                category = category,
                                author = author,
                                url = contentUrl.takeIf { it.isNotBlank() },
                                thumbnail = thumbnailUrl.takeIf { it.isNotBlank() }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = canSubmit && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = LocalContentColor.current)
                    } else {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}

@Composable
fun FileSelector(selectedFileUri: Uri?, onFileSelect: () -> Unit) {
    val context = LocalContext.current
    val fileName = selectedFileUri?.let { getFileNameFromUri(context, it) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onFileSelect) {
            Text("Seleccionar archivo")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = fileName ?: "Ningún archivo seleccionado.")
    }
}

@Composable
fun RadioButtonWithText(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddGalleryItemScreenPreview() {
    LenguaTheme {
        AddGalleryItemScreen(onCancel = {})
    }
}
