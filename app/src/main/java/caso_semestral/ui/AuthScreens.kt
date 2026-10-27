package com.example.caso_semestral.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.caso_semestral.ColorDocente
import com.example.caso_semestral.Encabezado
import com.example.caso_semestral.VerdeInstitucional
import com.example.caso_semestral.data.CatalogoAsignaturas
import com.example.caso_semestral.model.Rol
import com.example.caso_semestral.validarAliasPorRol
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
        EncabezadoLogo("App de estudio", "Iniciar sesión")

        Aparece {
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
                    supportingText = { MensajeAnimado(ui.error) },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { vm.iniciarSesion(alias, password) },
                    enabled = validarLogin(alias, password) && !ui.cargando,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
                ) {
                    AnimatedContent(targetState = ui.cargando, label = "botonEntrar") { cargando ->
                        if (cargando) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("Entrar")
                        }
                    }
                }
                TextButton(onClick = onIrARegistro, modifier = Modifier.fillMaxWidth()) {
                    Text("¿No tienes cuenta? Crear cuenta", color = VerdeInstitucional)
                }
            }
        }
    }
}

// Registro en 2 pasos (igual para estudiante y docente):
//   Paso 1: rol, alias y contraseña (el docente debe usar un alias Docente_[nombre])
//   Paso 2: asignaturas (las que cursa el estudiante / las que imparte el docente)
@Composable
fun RegistroScreen(vm: AuthViewModel, onVolver: () -> Unit) {
    val ui by vm.ui.collectAsState()
    var paso by remember { mutableStateOf(1) }
    var rol by remember { mutableStateOf(Rol.ESTUDIANTE) }
    var alias by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }
    var aliasEnUso by remember { mutableStateOf(false) }
    var verificando by remember { mutableStateOf(false) }
    val marcadas = remember { mutableStateListOf<Int>() }
    val asignaturas = remember { CatalogoAsignaturas.todas() }

    val esDocente = rol == Rol.DOCENTE
    val color = if (esDocente) ColorDocente else VerdeInstitucional

    LaunchedEffect(Unit) { vm.limpiarError() }

    // en el paso 2, "atrás" vuelve al paso 1 en vez de salir del registro
    BackHandler(enabled = paso == 2) { paso = 1 }

    if (paso == 1) {
        val aliasMalo = alias.isNotEmpty() && alias.length < 4
        val passMala = password.isNotEmpty() && password.length < 8
        val repetirMalo = repetir.isNotEmpty() && repetir != password
        val aliasOk = validarAliasPorRol(alias, rol)
        // error según el rol (solo si ya escribió algo)
        val errorRol: String? = when {
            alias.isEmpty() || aliasOk -> null
            esDocente -> "Debe tener el formato Docente_[nombre]"
            else -> "El prefijo Docente_ es solo para docentes"
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Encabezado("Paso 1 de 2", "Crear cuenta", color)

            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Soy…", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = rol == Rol.ESTUDIANTE,
                        onClick = { rol = Rol.ESTUDIANTE },
                        label = { Text("Estudiante") }
                    )
                    FilterChip(
                        selected = rol == Rol.DOCENTE,
                        onClick = { rol = Rol.DOCENTE },
                        label = { Text("Docente") }
                    )
                }

                // mensaje que ve solo el docente
                if (esDocente) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ColorDocente.copy(alpha = 0.15f))
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Cuenta de docente", fontWeight = FontWeight.Bold, color = ColorDocente)
                            Text(
                                "Para crear una cuenta de docente, tu alias debe ser Docente_[nombre]. " +
                                        "Ejemplo: Docente_Maria",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it; aliasEnUso = false; vm.limpiarError() },
                    label = { Text(if (esDocente) "Alias (Docente_[nombre])" else "Alias (mínimo 4 caracteres)") },
                    singleLine = true,
                    isError = aliasMalo || aliasEnUso || errorRol != null,
                    supportingText = {
                        if (aliasEnUso) Text("Ese alias ya está en uso")
                        else if (errorRol != null) Text(errorRol)
                        else if (aliasMalo) Text("Muy corto")
                    },
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

                Button(
                    onClick = {
                        verificando = true
                        vm.comprobarAlias(alias) { libre ->
                            verificando = false
                            if (libre) paso = 2 else aliasEnUso = true
                        }
                    },
                    enabled = validarRegistro(alias, password, repetir) && aliasOk && !verificando,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    Text("Continuar")
                }
                TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                    Text("Ya tengo cuenta", color = color)
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Encabezado(
                "Paso 2 de 2",
                if (esDocente) "Asignaturas que impartes" else "Elige tus asignaturas",
                color
            )

            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                asignaturas.forEach { asignatura ->
                    val marcada = asignatura.id in marcadas
                    val alternar = {
                        if (marcada) marcadas.remove(asignatura.id) else marcadas.add(asignatura.id)
                        Unit
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { alternar() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = marcada, onCheckedChange = { alternar() })
                            Text(asignatura.nombre, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Text(
                    if (esDocente) "Podrás subir material solo en las asignaturas que impartes."
                    else "Elige al menos una asignatura.",
                    style = MaterialTheme.typography.bodySmall
                )

                ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = { vm.registrar(alias, password, repetir, rol, marcadas.toList()) },
                    enabled = marcadas.isNotEmpty() && !ui.cargando,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    if (ui.cargando) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text(if (esDocente) "Crear cuenta de docente" else "Empezar a estudiar")
                    }
                }
                TextButton(onClick = { paso = 1 }, modifier = Modifier.fillMaxWidth()) {
                    Text("Atrás", color = color)
                }
            }
        }
    }
}