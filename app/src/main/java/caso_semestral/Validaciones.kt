package com.example.caso_semestral


// Validaciones de los formularios (misma idea que validarCampos de la guía)

fun validarLogin(usuario: String, password: String): Boolean {
    return usuario.isNotBlank() && password.isNotBlank()
}

fun validarRegistro(alias: String, password: String, repetir: String): Boolean {
    return alias.length >= 4 &&      // alias mínimo 4 caracteres
            password.length >= 8 &&  // contraseña mínimo 8 caracteres
            password == repetir      // las dos contraseñas deben coincidir
}