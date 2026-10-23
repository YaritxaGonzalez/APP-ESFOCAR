package com.example.caso_semestral.model

data class Contenido(
    val id: Int,
    val titulo: String,
    val texto: String,
    val unidad: Unidad
)