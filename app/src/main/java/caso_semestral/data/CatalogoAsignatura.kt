package com.example.caso_semestral.data

import com.example.caso_semestral.model.Asignatura

// Lista de asignaturas disponibles para elegir al registrarse (estudiante: las que cursa,
// docente: las que imparte). Por ahora sale de la base simulada.
object CatalogoAsignaturas {
    fun todas(): List<Asignatura> = BaseDatosSimulada().getAsignaturas()
}