package com.example.caso_semestral


// PANTALLA 3: ELEGIR ASIGNATURAS (paso 2 de 2)
//   Empezar a estudiar -> guarda las asignaturas y va a InicioActivity
//                         (borra el historial del registro)

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caso_semestral.data.UsuarioRepository

class AsignaturasActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alias = aliasActual()
        setContent { PantallaAsignaturas(alias) }
    }
}

@Composable
fun PantallaAsignaturas(alias: String) {
    val context = LocalContext.current
    val repository = remember { UsuarioRepository() }

    // la lista sale del repositorio, ya no está escrita a mano
    val listaAsignaturas = remember { repository.obtenerAsignaturas() }

    // mapa id de la asignatura -> marcada (cada cambio actualiza la pantalla)
    val marcadas = remember { mutableStateMapOf<Int, Boolean>() }

    Column(modifier = Modifier.fillMaxSize()) {
        Encabezado("Paso 2 de 2", "Elige tus asignaturas")

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listaAsignaturas.forEach { asignatura ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = marcadas[asignatura.id] == true,
                            onCheckedChange = { marcadas[asignatura.id] = it }
                        )
                        Text(asignatura.nombre, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Text("Puedes cambiarlas después en Ajustes.", fontSize = 12.sp)

            Button(
                onClick = {
                    // guarda en el estudiante las asignaturas que marcó (paso 2)
                    val elegidas = listaAsignaturas.filter { marcadas[it.id] == true }
                    repository.actualizarAsignaturas(alias, elegidas)

                    val intent = Intent(context, InicioActivity::class.java)
                    intent.putExtra("alias", alias)
                    // borra el historial: "atrás" ya no vuelve al registro
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                },
                enabled = marcadas.values.any { it }, // al menos una elegida
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
            ) {
                Text("Empezar a estudiar")
            }
        }
    }
}