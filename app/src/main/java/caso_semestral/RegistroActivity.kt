package com.example.caso_semestral
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.caso_semestral.data.UsuarioRepository

class RegistroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PantallaRegistro() }
    }
}

@Composable
fun PantallaRegistro() {
    val context = LocalContext.current
    val repository = remember { UsuarioRepository() }
    var alias by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }
    var aliasRepetido by remember { mutableStateOf(false) } // true = el alias ya existe

    Column(modifier = Modifier.fillMaxSize()) {
        Encabezado("Paso 1 de 2", "Crear cuenta")

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it; aliasRepetido = false },
                label = { Text("Alias (no uses tu nombre real)") },
                singleLine = true,
                isError = aliasRepetido,
                supportingText = {
                    if (aliasRepetido) Text("Ese alias ya existe, elige otro")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña (mínimo 8)") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = repetir,
                onValueChange = { repetir = it },
                label = { Text("Repetir contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = repetir.isNotEmpty() && repetir != password, // borde rojo si no coincide
                supportingText = {
                    if (repetir.isNotEmpty() && repetir != password) Text("Las contraseñas no coinciden")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Para este MVP no se piden RUT, correo ni fotos. Solo datos de prueba.", fontSize = 12.sp)

            Button(
                onClick = {
                    if (repository.existeAlias(alias)) {
                        aliasRepetido = true // alias ocupado: no avanza
                    } else {
                        repository.registrar(alias, password) // guarda la cuenta (paso 1)
                        context.irA(AsignaturasActivity::class.java, alias)
                    }
                },
                enabled = validarRegistro(alias, password, repetir),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
            ) {
                Text("Continuar")
            }
        }
    }
}