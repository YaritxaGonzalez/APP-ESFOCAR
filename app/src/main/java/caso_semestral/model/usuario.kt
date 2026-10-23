package com.example.caso_semestral.model

import java.time.LocalDate

// La cuenta de acceso. Solo datos ficticios: sin RUT, correo ni fotos reales.
data class Usuario(
    val codigo: Int,
    val alias: String,
    val passwordHash: String,   // la contraseña guardada como hash, nunca en texto plano
    val fechaRegistro: LocalDate,
    val rol: Rol
)