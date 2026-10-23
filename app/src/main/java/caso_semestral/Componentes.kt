package com.example.caso_semestral


// Piezas compartidas: colores, franja de arriba y función para navegar.

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- Colores (aproximados; cámbialos aquí y se actualiza toda la app) ----------
val VerdeInstitucional = Color(0xFF1F4D2B) // encabezados y botones del estudiante
val ColorDocente = Color(0xFF7A5C00)       // dorado oscuro: todo lo del docente

// ---------- Navegar a otra pantalla llevando el alias ----------
fun Context.irA(destino: Class<*>, alias: String) {
    val intent = Intent(this, destino)
    intent.putExtra("alias", alias) // el alias viaja a la siguiente pantalla
    startActivity(intent)
}

// ---------- Leer el alias que llegó en el Intent ----------
fun ComponentActivity.aliasActual(): String {
    return intent.getStringExtra("alias") ?: ""
}

// ---------- Franja de arriba ----------
// si no le pasas color, usa el verde (el docente pasa ColorDocente)
@Composable
fun Encabezado(subtitulo: String, titulo: String, color: Color = VerdeInstitucional) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Text(text = subtitulo, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        Text(text = titulo, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}