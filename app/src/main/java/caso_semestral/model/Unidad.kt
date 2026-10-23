package com.example.caso_semestral.model

// Una unidad de una asignatura (por ejemplo "Unidad 1 · Fundamentos")
data class Unidad(
    val id: Int,
    val nombre: String,
    val asignatura: Asignatura,
    val descargada: Boolean   // true = el contenido está guardado en el celular (sirve sin internet)
)