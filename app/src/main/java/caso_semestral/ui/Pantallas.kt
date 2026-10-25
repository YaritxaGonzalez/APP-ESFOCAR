package com.example.caso_semestral.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.caso_semestral.VerdeInstitucional

@Composable
fun InicioScreen(alias: String) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Bienvenido, $alias", style = MaterialTheme.typography.headlineSmall)
        Text("Desde Documentos puedes cargar y consultar tu material de estudio, incluso sin conexión.")
    }
}

@Composable
fun PerfilScreen(alias: String, onCerrarSesion: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineSmall)
        Text("Usuario: $alias")
        Button(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
        ) { Text("Cerrar sesión") }
    }
}