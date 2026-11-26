package esfocar_api.admin

import esfocar_api.auth.UsuarioRepository
import esfocar_api.auth.UsuarioResponse
import esfocar_api.catalogo.CatalogoRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class NuevoUsuarioRequest(
    val alias: String,
    val password: String,
    val rol: String,                    // ESTUDIANTE o DOCENTE
    val codigoSeccion: String? = null   // obligatorio para ESTUDIANTE, no se usa en DOCENTE
)

// asignaturaId = null: el docente dicta todas las asignaturas en esa sección
data class AsignacionRequest(val docente: String, val codigoSeccion: String, val asignaturaId: Long? = null)

// toda la ruta /api/admin/** ya exige rol ADMIN (ver ConfigSeguridad)
@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val usuarios: UsuarioRepository,
    private val catalogo: CatalogoRepository,
    private val encoder: PasswordEncoder
) {
    private val regexAlias = Regex("^[A-Za-z0-9_.-]{4,30}$")

    private fun error(estado: HttpStatus, mensaje: String): ResponseEntity<Any> =
        ResponseEntity.status(estado).body(mapOf("error" to mensaje))

    @PostMapping("/usuarios")
    fun crearUsuario(@RequestBody req: NuevoUsuarioRequest): ResponseEntity<Any> {
        val alias = req.alias.trim()
        val rol = req.rol.trim().uppercase()

        if (!regexAlias.matches(alias))
            return error(HttpStatus.BAD_REQUEST, "El alias debe tener de 4 a 30 caracteres: letras, números, punto, guion o guion bajo")
        if (req.password.length !in 8..64)
            return error(HttpStatus.BAD_REQUEST, "La contraseña debe tener entre 8 y 64 caracteres")
        if (rol != "ESTUDIANTE" && rol != "DOCENTE")
            return error(HttpStatus.BAD_REQUEST, "El rol debe ser ESTUDIANTE o DOCENTE")

        val seccionId: Long? = if (rol == "ESTUDIANTE") {
            val codigo = req.codigoSeccion?.trim()?.uppercase()
                ?: return error(HttpStatus.BAD_REQUEST, "El estudiante necesita un código de sección")
            usuarios.seccionIdPorCodigo(codigo) ?: return error(HttpStatus.BAD_REQUEST, "Código de sección no válido")
        } else null

        if (usuarios.buscarPorAlias(alias) != null)
            return error(HttpStatus.CONFLICT, "Ese alias ya está en uso")

        return try {
            val id = usuarios.crear(alias, encoder.encode(req.password)!!, rol, seccionId)
            ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse(id, alias, rol, seccionId))
        } catch (e: DuplicateKeyException) {
            error(HttpStatus.CONFLICT, "Ese alias ya está en uso")
        }
    }

    @PostMapping("/asignaciones")
    fun asignarDocente(@RequestBody req: AsignacionRequest): ResponseEntity<Any> {
        val docente = usuarios.buscarPorAlias(req.docente.trim())
        if (docente == null || docente.rol != "DOCENTE")
            return error(HttpStatus.BAD_REQUEST, "No existe un docente con ese alias")

        val seccionId = usuarios.seccionIdPorCodigo(req.codigoSeccion.trim().uppercase())
            ?: return error(HttpStatus.BAD_REQUEST, "Código de sección no válido")

        if (req.asignaturaId != null && !catalogo.asignaturaExiste(req.asignaturaId))
            return error(HttpStatus.BAD_REQUEST, "La asignatura no existe")

        val creadas = usuarios.asignarDocente(docente.id, seccionId, req.asignaturaId)
        return ResponseEntity.ok(mapOf("asignacionesCreadas" to creadas))
    }
}