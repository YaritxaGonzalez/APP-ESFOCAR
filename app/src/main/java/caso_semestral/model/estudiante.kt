package com.example.caso_semestral.model


data class Estudiante(
    val codigo: Int,
    val usuario: Usuario,                 // su cuenta de acceso
    val asignaturas: List<Asignatura>     // las que eligió en el paso 2 del registro
){
    // igual que calcularPrecioConIva() en Producto: una función dentro del modelo
    fun cantidadAsignaturas(): Int {
        return asignaturas.size
    }
}