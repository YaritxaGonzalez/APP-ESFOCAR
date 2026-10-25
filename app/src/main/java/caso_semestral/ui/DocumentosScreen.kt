package com.example.caso_semestral.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.caso_semestral.VerdeInstitucional
import com.example.caso_semestral.data.local.DocumentoEntity
import com.example.caso_semestral.viewmodel.DocumentosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// tipos que la app sabe visualizar
private val TIPOS_PERMITIDOS = arrayOf("application/pdf", "image/*", "text/plain")

@Composable
fun DocumentosScreen(vm: DocumentosViewModel, onAbrir: (Int) -> Unit) {
    val documentos by vm.documentos.collectAsState()
    val ocupado by vm.ocupado.collectAsState()
    var porEliminar by remember { mutableStateOf<DocumentoEntity?>(null) }

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.cargar(it) }
    }

    Box(Modifier.fillMaxSize()) {

        if (documentos.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text("Aún no tienes documentos", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Carga PDF, imágenes o archivos de texto para estudiar sin conexión.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(documentos, key = { it.id }) { doc ->
                    Card(onClick = { onAbrir(doc.id) }, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(iconoPara(doc.mime), contentDescription = null, tint = VerdeInstitucional)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    doc.nombre.substringAfter("_"),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "${formatearTamano(doc.tamanoBytes)} · ${formatearFecha(doc.fecha)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { porEliminar = doc }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }

        if (ocupado) {
            LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
        }

        ExtendedFloatingActionButton(
            onClick = { selector.launch(TIPOS_PERMITIDOS) },
            icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
            text = { Text("Cargar") },
            containerColor = VerdeInstitucional,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    porEliminar?.let { doc ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar documento") },
            text = { Text("¿Eliminar \"${doc.nombre.substringAfter("_")}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.eliminar(doc)
                    porEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

private fun iconoPara(mime: String): ImageVector = when {
    mime == "application/pdf" -> Icons.Default.PictureAsPdf
    mime.startsWith("image/") -> Icons.Default.Image
    else -> Icons.Default.Description
}

private fun formatearTamano(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
}

private fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(millis))