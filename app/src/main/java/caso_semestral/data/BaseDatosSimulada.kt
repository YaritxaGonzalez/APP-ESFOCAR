package com.example.caso_semestral.data

import com.example.caso_semestral.model.Asignatura
import com.example.caso_semestral.model.Contenido
import com.example.caso_semestral.model.Docente
import com.example.caso_semestral.model.Estudiante
import com.example.caso_semestral.model.Rol
import com.example.caso_semestral.model.Unidad
import com.example.caso_semestral.model.Usuario
import java.time.LocalDate

class BaseDatosSimulada {

    // companion object = las listas se comparten entre todas las pantallas
    companion object {
        private val asignaturaA = Asignatura( id = 1, nombre = "Asignatura demo A" )
        private val asignaturaB = Asignatura( id = 2, nombre = "Asignatura demo B" )
        private val asignaturaC = Asignatura( id = 3, nombre = "Asignatura demo C" )

        private val asignaturas = listOf(asignaturaA, asignaturaB, asignaturaC)

        // ---------- Unidades (descargada = false se ve bloqueada) ----------
        private val unidadA1 = Unidad( id = 1, nombre = "Unidad 1 · Fundamentos", asignatura = asignaturaA, descargada = true )
        private val unidadA2 = Unidad( id = 2, nombre = "Unidad 2 · Procedimientos", asignatura = asignaturaA, descargada = true )
        private val unidadA3 = Unidad( id = 3, nombre = "Unidad 3 · Terreno", asignatura = asignaturaA, descargada = false )
        private val unidadB1 = Unidad( id = 4, nombre = "Unidad 1 · Introducción", asignatura = asignaturaB, descargada = true )
        private val unidadB2 = Unidad( id = 5, nombre = "Unidad 2 · Casos aplicados", asignatura = asignaturaB, descargada = false )
        private val unidadC1 = Unidad( id = 6, nombre = "Unidad 1 · Conceptos", asignatura = asignaturaC, descargada = true )

        private val unidades = listOf(unidadA1, unidadA2, unidadA3, unidadB1, unidadB2, unidadC1)

        // ---------- Contenidos ficticios ----------
        private val contenidos = listOf(
            Contenido( id = 1, titulo = "¿Qué es un procedimiento?", texto = "Un procedimiento es una secuencia de pasos ordenados que se siguen para realizar una tarea. Contenido ficticio de ejemplo.", unidad = unidadA1 ),
            Contenido( id = 2, titulo = "Importancia de registrar", texto = "Registrar los hechos permite revisarlos después y mejorar la forma de actuar. Contenido ficticio de ejemplo.", unidad = unidadA1 ),
            Contenido( id = 3, titulo = "Pasos básicos", texto = "Primero se verifica la información, luego se registra y finalmente se informa. Contenido ficticio de ejemplo.", unidad = unidadA2 ),
            Contenido( id = 4, titulo = "Errores frecuentes", texto = "No verificar los datos y omitir pasos son los errores más comunes. Contenido ficticio de ejemplo.", unidad = unidadA2 ),
            Contenido( id = 5, titulo = "Introducción al tema", texto = "Presentación general de los conceptos de la asignatura B. Contenido ficticio de ejemplo.", unidad = unidadB1 ),
            Contenido( id = 6, titulo = "Conceptos clave", texto = "Definiciones principales de la asignatura C. Contenido ficticio de ejemplo.", unidad = unidadC1 )
        )

        // ---------- Usuarios de prueba ----------
        // estudiante de prueba: alias cadete_romero, contraseña 123456Abcdef
        private val usuarioEstudiante = Usuario(
            codigo = 1,
            alias = "cadete_romero",
            passwordHash = "123456Abcdef",
            fechaRegistro = LocalDate.of(2026, 10, 9),
            rol = Rol.ESTUDIANTE
        )

        // docente de prueba: alias docente_demo, contraseña 12345678
        private val usuarioDocente = Usuario(
            codigo = 2,
            alias = "docente_demo",
            passwordHash = "12345678",
            fechaRegistro = LocalDate.of(2026, 10, 9),
            rol = Rol.DOCENTE
        )

        private val usuarios = mutableListOf(usuarioEstudiante, usuarioDocente)

        private val estudiantes = mutableListOf(
            Estudiante(codigo = 1, usuario = usuarioEstudiante, asignaturas = listOf(asignaturaA, asignaturaB))
        )

        private val docentes = mutableListOf(
            Docente(codigo = 1, usuario = usuarioDocente, asignaturas = listOf(asignaturaA, asignaturaB))
        )
    }

    fun getAsignaturas(): List<Asignatura> {
        return asignaturas
    }

    fun getUnidades(): List<Unidad> {
        return unidades
    }

    fun getContenidos(): List<Contenido> {
        return contenidos
    }

    fun getUsuarios(): List<Usuario> {
        return usuarios
    }

    fun getEstudiantes(): List<Estudiante> {
        return estudiantes
    }

    fun getDocentes(): List<Docente> {
        return docentes
    }

    fun addUsuario(usuario: Usuario) {
        usuarios.add(usuario)
    }

    fun addEstudiante(estudiante: Estudiante) {
        estudiantes.add(estudiante)
    }

    // reemplaza al estudiante que tiene el mismo código (se usa en el paso 2 del registro)
    fun updateEstudiante(estudiante: Estudiante) {
        val posicion = estudiantes.indexOfFirst { it.codigo == estudiante.codigo }
        if (posicion != -1) estudiantes[posicion] = estudiante
    }
}