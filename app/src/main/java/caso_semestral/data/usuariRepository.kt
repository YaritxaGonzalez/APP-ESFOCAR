package com.example.caso_semestral.data

import com.example.caso_semestral.model.Asignatura
import com.example.caso_semestral.model.Docente
import com.example.caso_semestral.model.Estudiante
import com.example.caso_semestral.model.Rol
import com.example.caso_semestral.model.Usuario
import java.time.LocalDate
import com.example.caso_semestral.model.Contenido
import com.example.caso_semestral.model.Unidad

class UsuarioRepository {

    private val baseDatos = BaseDatosSimulada()

    fun obtenerAsignaturas(): List<Asignatura> {
        return baseDatos.getAsignaturas()
    }

    // unidades que pertenecen a una asignatura
    fun obtenerUnidades(asignatura: Asignatura): List<Unidad> {
        return baseDatos.getUnidades().filter { it.asignatura.id == asignatura.id }
    }

    // contenidos que pertenecen a una unidad
    fun obtenerContenidos(unidad: Unidad): List<Contenido> {
        return baseDatos.getContenidos().filter { it.unidad.id == unidad.id }
    }

    // busca la cuenta por alias (de ahí sale el rol)
    fun obtenerUsuario(alias: String): Usuario? {
        return baseDatos.getUsuarios().find { it.alias == alias }
    }

    // busca al estudiante por el alias de su cuenta
    fun obtenerEstudiante(alias: String): Estudiante? {
        return baseDatos.getEstudiantes().find { it.usuario.alias == alias }
    }

    // busca al docente por el alias de su cuenta
    fun obtenerDocente(alias: String): Docente? {
        return baseDatos.getDocentes().find { it.usuario.alias == alias }
    }

    // el alias debe ser único
    fun existeAlias(alias: String): Boolean {
        return baseDatos.getUsuarios().any { it.alias == alias }
    }

    // PASO 1 del registro: crea la cuenta y el estudiante (todavía sin asignaturas)
    fun registrar(alias: String, password: String): Boolean {
        if (existeAlias(alias)) return false // alias repetido

        val usuario = Usuario(
            codigo = baseDatos.getUsuarios().size + 1,
            alias = alias,
            passwordHash = password, // ← CAMBIO: se guarda tal cual la escribió el usuario
            fechaRegistro = LocalDate.now(),
            rol = Rol.ESTUDIANTE
        )
        baseDatos.addUsuario(usuario)
        baseDatos.addEstudiante(
            Estudiante(
                codigo = baseDatos.getEstudiantes().size + 1,
                usuario = usuario,
                asignaturas = emptyList()
            )
        )
        return true
    }

    // PASO 2 del registro: le agrega las asignaturas elegidas
    fun actualizarAsignaturas(alias: String, asignaturas: List<Asignatura>) {
        val actual = obtenerEstudiante(alias) ?: return
        baseDatos.updateEstudiante(actual.copy(asignaturas = asignaturas)) // copy = mismo estudiante con otro dato
    }

    // login: el alias existe y la contraseña coincide
    fun iniciarSesion(alias: String, password: String): Boolean {
        return baseDatos.getUsuarios().any {
            it.alias == alias && it.passwordHash == password // ← CAMBIO: compara directo, sin hash
        }
    }
}