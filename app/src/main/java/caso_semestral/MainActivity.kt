package com.example.caso_semestral

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.caso_semestral.data.UsuarioRepository
import com.example.caso_semestral.ui.theme.Caso_semestralTheme
import kotlin.jvm.java

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Login() }
    }
}

@Composable
fun Login() {
    val context = LocalContext.current
    val repository = remember { UsuarioRepository() }
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorLogin by remember { mutableStateOf(false) } // true = datos incorrectos

    Column(modifier = Modifier.fillMaxSize()) {
        Encabezado("App de estudio", "Iniciar sesión")

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = user,
                onValueChange = { user = it; errorLogin = false },
                label = { Text("Usuario") },
                singleLine = true,
                isError = errorLogin,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorLogin = false },
                label = { Text(text = "Contraseña") },
                visualTransformation = PasswordVisualTransformation(), // oculta la contraseña
                singleLine = true,
                isError = errorLogin,
                supportingText = {
                    if (errorLogin) Text("Usuario o contraseña incorrectos")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (repository.iniciarSesion(user, password)) {
                        context.irA(InicioActivity::class.java, user)
                    } else {
                        errorLogin = true // muestra el mensaje de error
                    }
                },
                enabled = validarLogin(user, password),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeInstitucional)
            ) {
                Text("Entrar")
            }

            TextButton(
                onClick = { context.irA(RegistroActivity::class.java, "") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("¿No tienes cuenta? Crear cuenta", color = VerdeInstitucional)
            }
        }
    }
}

