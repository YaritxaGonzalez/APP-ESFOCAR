package com.example.caso_semestral

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caso_semestral.data.UsuarioRepository
import com.example.caso_semestral.model.Contenido
import com.example.caso_semestral.model.Unidad

class EstudiarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alias = aliasActual()
        setContent { PantallaEstudiar(alias) }
    }
}

@Composable
fun PantallaEstudiar(alias: String) {
    val repository = remember { UsuarioRepository() }
    val estudiante = remember { repository.obtenerEstudiante(alias) }
    val asignaturas = estudiante?.asignaturas ?: emptyList()

    // id de la unidad que está desplegada (-1 = ninguna)
    var abierta by remember { mutableStateOf(-1) }

    Column(modifier = Modifier.fillMaxSize()) {
        Encabezado("Contenidos", "Estudiar")

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()) // permite bajar si hay muchas unidades
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (asignaturas.isEmpty()) {
                Text("Aún no tienes asignaturas elegidas.")
            }

            asignaturas.forEach { asignatura ->
                Text(asignatura.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                repository.obtenerUnidades(asignatura).forEach { unidad ->
                    TarjetaUnidad(
                        unidad = unidad,
                        contenidos = repository.obtenerContenidos(unidad),
                        desplegada = abierta == unidad.id,
                        alTocar = {
                            // si ya estaba abierta la cierra; si no, abre esta
                            abierta = if (abierta == unidad.id) -1 else unidad.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaUnidad(
    unidad: Unidad,
    contenidos: List<Contenido>,
    desplegada: Boolean,
    alTocar: () -> Unit
) {
    Card(
        onClick = alTocar,
        enabled = unidad.descargada, // si no está descargada no se puede abrir
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (unidad.descargada) Color.White else Color(0xFFE6E8E6),
            disabledContainerColor = Color(0xFFE6E8E6)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(unidad.nombre, fontWeight = FontWeight.Bold)

            if (unidad.descargada) {
                Text("${contenidos.size} contenidos · Descargada", fontSize = 12.sp)
            } else {
                Text("Requiere conexión para descargar", fontSize = 12.sp)
            }

            // contenidos de la unidad (solo si está desplegada)
            if (desplegada) {
                contenidos.forEach { contenido ->
                    Text(
                        text = contenido.titulo,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(contenido.texto, fontSize = 14.sp)
                }
            }
        }
    }
}