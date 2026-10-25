package com.example.caso_semestral.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.caso_semestral.Encabezado
import com.example.caso_semestral.VerdeInstitucional
import com.example.caso_semestral.validarLogin
import com.example.caso_semestral.validarRegistro
import com.example.caso_semestral.viewmodel.AuthViewModel

@Composable
fun LoginScreen(vm: AuthViewModel, onIrARegistro: () -> Unit) {
    val ui by vm.ui.collectAsState()
    var alias by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.limpiarError() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Encabezado("App de estudio", "Iniciar sesión")

        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it; vm.limpiarError() },
                label = { Text("Usuario") },
                singleLine = true,
                isError = ui.error != null,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; vm.limpiarError() },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = ui.error != null,
                supportingText = { ui.error?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { vm.iniciarSesion(alias, password) },
                enabled = validarLogin(alias, password) && !ui.cargando,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
            ) {
                if (ui.cargando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Entrar")
                }
            }
            TextButton(onClick = onIrARegistro, modifier = Modifier.fillMaxWidth()) {
                Text("¿No tienes cuenta? Crear cuenta", color = VerdeInstitucional)
            }
        }
    }
}

@Composable
fun RegistroScreen(vm: AuthViewModel, onVolver: () -> Unit) {
    val ui by vm.ui.collectAsState()
    var alias by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.limpiarError() }

    val aliasMalo = alias.isNotEmpty() && alias.length < 4
    val passMala = password.isNotEmpty() && password.length < 8
    val repetirMalo = repetir.isNotEmpty() && repetir != password

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Encabezado("Nuevo usuario", "Crear cuenta")

        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it; vm.limpiarError() },
                label = { Text("Alias (mínimo 4 caracteres)") },
                singleLine = true,
                isError = aliasMalo || ui.error != null,
                supportingText = { if (aliasMalo) Text("Muy corto") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña (mínimo 8 caracteres)") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = passMala,
                supportingText = { if (passMala) Text("Muy corta") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = repetir,
                onValueChange = { repetir = it },
                label = { Text("Repetir contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = repetirMalo,
                supportingText = { if (repetirMalo) Text("Las contraseñas no coinciden") },
                modifier = Modifier.fillMaxWidth()
            )

            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = { vm.registrar(alias, password, repetir) },
                enabled = validarRegistro(alias, password, repetir) && !ui.cargando,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
            ) {
                if (ui.cargando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Registrarme")
                }
            }
            TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Ya tengo cuenta", color = VerdeInstitucional)
            }
        }
    }
}