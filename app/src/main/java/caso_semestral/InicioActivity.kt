package com.example.caso_semestral


// PANTALLA 4: INICIO
// Según el rol de la cuenta se muestra una pantalla distinta:
//   ESTUDIANTE -> PantallaEstudiante (franja verde)
//   DOCENTE    -> PantallaDocente    (franja dorada)

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caso_semestral.data.UsuarioRepository
import com.example.caso_semestral.model.Rol
import androidx.compose.ui.platform.LocalContext

class InicioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alias = aliasActual()
        setContent { PantallaInicio(alias) }
    }
}

@Composable
fun PantallaInicio(alias: String) {
    val repository = remember { UsuarioRepository() }
    val usuario = remember { repository.obtenerUsuario(alias) }

    // aquí se decide qué pantalla ve cada uno
    if (usuario?.rol == Rol.DOCENTE) {
        PantallaDocente(alias, repository)
    } else {
        PantallaEstudiante(alias, repository)
    }
}

// ---------- Lo que ve el ESTUDIANTE ----------
@Composable
fun PantallaEstudiante(alias: String, repository: UsuarioRepository) {
    val estudiante = remember { repository.obtenerEstudiante(alias) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Encabezado("Estudiante", "Hola, $alias") // franja verde

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mis asignaturas (${estudiante?.cantidadAsignaturas() ?: 0})", fontWeight = FontWeight.Bold)
                    estudiante?.asignaturas?.forEach { Text("· ${it.nombre}", fontSize = 14.sp) }
                }
            }
            BotonRol("Estudiar", VerdeInstitucional) { context.irA(EstudiarActivity::class.java, alias) }

            BotonRol("Practicar", VerdeInstitucional) { /* TODO: ir a Practicar */ }
            BotonRol("Mi avance", VerdeInstitucional) { /* TODO: ir a Avance */ }
        }
    }
}

// ---------- Lo que ve el DOCENTE ----------
@Composable
fun PantallaDocente(alias: String, repository: UsuarioRepository) {
    val docente = remember { repository.obtenerDocente(alias) }

    Column(modifier = Modifier.fillMaxSize()) {
        Encabezado("Docente", "Panel docente · $alias", ColorDocente) // franja dorada

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Asignaturas que impartes (${docente?.cantidadAsignaturas() ?: 0})", fontWeight = FontWeight.Bold)
                    docente?.asignaturas?.forEach { Text("· ${it.nombre}", fontSize = 14.sp) }
                }
            }

            BotonRol("Crear pregunta", ColorDocente) { /* TODO: ir a crear pregunta */ }
            BotonRol("Crear caso simulado", ColorDocente) { /* TODO: ir a crear caso */ }
            BotonRol("Resultados de estudiantes", ColorDocente) { /* TODO: ir a resultados */ }
        }
    }
}

// Botón reutilizable: recibe el color para que cada rol tenga el suyo
@Composable
fun BotonRol(texto: String, color: Color, alTocar: () -> Unit) {
    Button(
        onClick = alTocar,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(texto)
    }
}