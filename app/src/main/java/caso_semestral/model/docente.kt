package com.example.caso_semestral.model

// El docente: una persona con cuenta de acceso (Usuario) y las asignaturas que imparte.
// Igual que Producto tiene una Categoria, Docente tiene un Usuario y una lista de Asignaturas.
data class Docente(
    val codigo: Int,
    val usuario: Usuario,                 // su cuenta de acceso (alias, contraseña con hash, rol DOCENTE)
    val asignaturas: List<Asignatura>     // las que imparte
){
    // igual que calcularPrecioConIva() en Producto: una función dentro del modelo
    fun cantidadAsignaturas(): Int {
        return asignaturas.size
    }

    // un docente solo debería crear contenido en las asignaturas que imparte
    fun imparte(asignatura: Asignatura): Boolean {
        return asignaturas.any { it.id == asignatura.id }
    }
}