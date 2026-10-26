package esfocar_api.auth

import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class RegistroRequest(val alias: String, val password: String, val codigoSeccion: String)
data class LoginRequest(val alias: String, val password: String)
data class UsuarioResponse(val id: Long, val alias: String, val rol: String, val seccionId: Long?)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val repo: UsuarioRepository,
    private val encoder: PasswordEncoder
) {
    private val regexAlias = Regex("^[A-Za-z0-9_.-]{4,30}$")

    // hash de relleno: se compara aun cuando el usuario no existe, para que
    // el tiempo de respuesta no revele qué alias están registrados
    private val hashRelleno: String = encoder.encode("relleno-no-se-usa")!!

    @PostMapping("/registro")
    fun registro(@RequestBody req: RegistroRequest): ResponseEntity<Any> {
        val alias = req.alias.trim()
        if (!regexAlias.matches(alias))
            return error(HttpStatus.BAD_REQUEST, "El alias debe tener de 4 a 30 caracteres: letras, números, punto, guion o guion bajo")
        if (req.password.length !in 8..64)
            return error(HttpStatus.BAD_REQUEST, "La contraseña debe tener entre 8 y 64 caracteres")

        val seccionId = repo.seccionIdPorCodigo(req.codigoSeccion.trim().uppercase())
            ?: return error(HttpStatus.BAD_REQUEST, "Código de sección no válido")

        if (repo.buscarPorAlias(alias) != null)
            return error(HttpStatus.CONFLICT, "Ese alias ya está en uso")

        return try {
            val id = repo.crear(alias, encoder.encode(req.password)!!, "ESTUDIANTE", seccionId)
            ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse(id, alias, "ESTUDIANTE", seccionId))
        } catch (e: DuplicateKeyException) {
            error(HttpStatus.CONFLICT, "Ese alias ya está en uso")
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<Any> {
        val usuario = repo.buscarPorAlias(req.alias.trim())
        val hash = usuario?.passwordHash ?: hashRelleno
        val coincide = encoder.matches(req.password, hash)

        if (usuario == null || !coincide)
            return error(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos")

        return ResponseEntity.ok(UsuarioResponse(usuario.id, usuario.alias, usuario.rol, usuario.seccionId))
    }

    private fun error(estado: HttpStatus, mensaje: String): ResponseEntity<Any> =
        ResponseEntity.status(estado).body(mapOf("error" to mensaje))
}