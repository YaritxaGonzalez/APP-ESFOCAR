package com.example.caso_semestral.ui

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO = "inicio"
    const val DOCUMENTOS = "documentos"
    const val PERFIL = "perfil"
    const val VISOR = "visor/{id}"

    fun visor(id: Int) = "visor/$id"

    val publicas = listOf(LOGIN, REGISTRO)
    val pestanas = listOf(INICIO, DOCUMENTOS, PERFIL)
}