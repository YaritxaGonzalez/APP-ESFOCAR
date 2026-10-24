package com.example.caso_semestral.data

import com.example.caso_semestral.data.local.UsuarioDao
import com.example.caso_semestral.data.local.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val usuarioDao: UsuarioDao) {

    // devuelve el id del usuario creado, o null si el alias ya existe
    suspend fun registrar(alias: String, password: String): Int? = withContext(Dispatchers.Default) {
        val aliasLimpio = alias.trim()
        val salt = Seguridad.nuevoSalt()
        val usuario = UsuarioEntity(
            alias = aliasLimpio,
            passwordHash = Seguridad.hash(password, salt),
            salt = salt
        )
        val id = usuarioDao.insertar(usuario)
        if (id == -1L) null else id.toInt()
    }

    // devuelve el id si las credenciales son correctas, o null
    suspend fun iniciarSesion(alias: String, password: String): Int? = withContext(Dispatchers.Default) {
        val usuario = usuarioDao.buscarPorAlias(alias.trim()) ?: return@withContext null
        if (Seguridad.coincide(password, usuario.salt, usuario.passwordHash)) usuario.id else null
    }

    suspend fun obtenerAlias(usuarioId: Int): String? = usuarioDao.buscarPorId(usuarioId)?.alias
}