package com.example.caso_semestral.data

import com.example.caso_semestral.data.local.UsuarioAsignaturaDao
import com.example.caso_semestral.data.local.UsuarioAsignaturaEntity
import com.example.caso_semestral.data.local.UsuarioDao
import com.example.caso_semestral.data.local.UsuarioEntity
import com.example.caso_semestral.data.remote.ClienteApi
import com.example.caso_semestral.data.remote.EsfocarApi
import com.example.caso_semestral.data.remote.LoginRequest
import com.example.caso_semestral.model.Asignatura
import com.example.caso_semestral.model.Rol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// Resultado del login contra el backend
sealed interface ResultadoLogin {
    data class Exito(val usuarioId: Int) : ResultadoLogin
    data object CredencialesInvalidas : ResultadoLogin   // 401
    data object SinConexion : ResultadoLogin             // sin red, servidor apagado o timeout
    data object RolNoSoportado : ResultadoLogin          // ej. ADMIN: la app no tiene pantalla para ese rol
    data object ErrorServidor : ResultadoLogin           // cualquier otra respuesta inesperada
}

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val asignaturaDao: UsuarioAsignaturaDao,
    private val api: EsfocarApi = ClienteApi.api
) {

    // paso 1 del registro: ¿el alias está libre?
    suspend fun aliasDisponible(alias: String): Boolean =
        usuarioDao.buscarPorAlias(alias.trim()) == null

    // crea la cuenta con su rol y guarda las asignaturas elegidas (paso 2)
    // devuelve el id del usuario creado, o null si el alias ya existe
    suspend fun registrar(
        alias: String,
        password: String,
        rol: Rol,
        asignaturaIds: List<Int>
    ): Int? = withContext(Dispatchers.Default) {
        val aliasLimpio = alias.trim()
        val salt = Seguridad.nuevoSalt()
        val usuario = UsuarioEntity(
            alias = aliasLimpio,
            passwordHash = Seguridad.hash(password, salt),
            salt = salt,
            rol = rol.name
        )
        val id = usuarioDao.insertar(usuario)
        if (id == -1L) {
            null
        } else {
            val usuarioId = id.toInt()
            asignaturaDao.insertar(
                asignaturaIds.distinct().map { UsuarioAsignaturaEntity(usuarioId, it) }
            )
            usuarioId
        }
    }

    // login contra el backend (POST /api/auth/login)
    suspend fun iniciarSesion(alias: String, password: String): ResultadoLogin {
        val respuesta = try {
            api.login(LoginRequest(alias.trim(), password))
        } catch (e: IOException) {
            return ResultadoLogin.SinConexion
        }

        if (respuesta.code() == 401) return ResultadoLogin.CredencialesInvalidas
        val usuario = respuesta.body()
        if (!respuesta.isSuccessful || usuario == null) return ResultadoLogin.ErrorServidor

        if (Rol.entries.none { it.name == usuario.rol }) return ResultadoLogin.RolNoSoportado

        return ResultadoLogin.Exito(usuarioLocal(usuario.alias, usuario.rol))
    }

    // El backend es quien autentica; Room queda como caché local (documentos, foto, asignaturas).
    // Si el alias ya existe localmente se reutiliza su id, así no se pierden sus datos.
    // La contraseña NO se guarda en el teléfono para las cuentas que vienen del backend.
    private suspend fun usuarioLocal(alias: String, rol: String): Int {
        val existente = usuarioDao.buscarPorAlias(alias)
        if (existente != null) {
            if (existente.rol != rol) usuarioDao.actualizarRol(existente.id, rol)
            return existente.id
        }
        val id = usuarioDao.insertar(
            UsuarioEntity(alias = alias, passwordHash = "", salt = "", rol = rol)
        )
        return id.toInt()
    }

    suspend fun obtenerAlias(usuarioId: Int): String? = usuarioDao.buscarPorId(usuarioId)?.alias

    // las cuentas anteriores a esta versión quedan como ESTUDIANTE
    suspend fun obtenerRol(usuarioId: Int): Rol {
        val texto = usuarioDao.buscarPorId(usuarioId)?.rol ?: return Rol.ESTUDIANTE
        return runCatching { Rol.valueOf(texto) }.getOrDefault(Rol.ESTUDIANTE)
    }

    suspend fun obtenerAsignaturas(usuarioId: Int): List<Asignatura> {
        val ids = asignaturaDao.idsDe(usuarioId)
        return CatalogoAsignaturas.todas().filter { it.id in ids }
    }
}