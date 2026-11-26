package esfocar_api.catalogo

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class CatalogoController(private val repo: CatalogoRepository) {

    private fun idDe(jwt: Jwt): Long? = jwt.subject?.toLongOrNull()
    private fun rolDe(jwt: Jwt): String? = jwt.getClaimAsString("rol")

    private fun error(estado: HttpStatus, mensaje: String): ResponseEntity<Any> =
        ResponseEntity.status(estado).body(mapOf("error" to mensaje))

    private fun sesionInvalida() = error(HttpStatus.UNAUTHORIZED, "Sesión no válida o expirada")

    // 404 y no 403: no se revela si la asignatura existe pero es de otra sección o docente
    private fun asignaturaNoEncontrada() = error(HttpStatus.NOT_FOUND, "Asignatura no encontrada")

    // ---------- asignaturas ----------

    @GetMapping("/asignaturas")
    fun asignaturas(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<Any> {
        val id = idDe(jwt) ?: return sesionInvalida()
        val lista = when (rolDe(jwt)) {
            "ESTUDIANTE" -> repo.seccionDeUsuario(id)?.let { repo.asignaturasDeSeccion(it) } ?: emptyList()
            "DOCENTE" -> repo.asignaturasDeDocente(id)
            "ADMIN" -> repo.todasLasAsignaturas()
            else -> return sesionInvalida()
        }
        return ResponseEntity.ok(lista)
    }

    // ---------- unidades ----------

    @GetMapping("/asignaturas/{asignaturaId}/unidades")
    fun unidades(@AuthenticationPrincipal jwt: Jwt, @PathVariable asignaturaId: Long): ResponseEntity<Any> {
        val id = idDe(jwt) ?: return sesionInvalida()
        return when (rolDe(jwt)) {
            "ESTUDIANTE" -> {
                val seccion = repo.seccionDeUsuario(id) ?: return asignaturaNoEncontrada()
                if (!repo.seccionDictaAsignatura(seccion, asignaturaId)) return asignaturaNoEncontrada()
                ResponseEntity.ok(repo.unidadesVisiblesParaSeccion(asignaturaId, seccion))
            }
            "DOCENTE" -> {
                if (!repo.docenteDictaAsignatura(id, asignaturaId)) return asignaturaNoEncontrada()
                ResponseEntity.ok(repo.unidadesParaGestion(asignaturaId, id))
            }
            "ADMIN" -> {
                if (!repo.asignaturaExiste(asignaturaId)) return asignaturaNoEncontrada()
                ResponseEntity.ok(repo.unidadesParaGestion(asignaturaId, null))
            }
            else -> sesionInvalida()
        }
    }

    // ---------- ocultar / mostrar una unidad en una sección (DOCENTE y ADMIN, ver ConfigSeguridad) ----------

    @PutMapping("/docente/secciones/{seccionId}/unidades/{unidadId}/oculta")
    fun ocultar(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable seccionId: Long,
        @PathVariable unidadId: Long
    ): ResponseEntity<Any> {
        val id = idDe(jwt) ?: return sesionInvalida()
        if (!puedeGestionar(jwt, id, seccionId, unidadId)) return unidadNoEncontrada()
        repo.ocultarUnidad(seccionId, unidadId, id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/docente/secciones/{seccionId}/unidades/{unidadId}/oculta")
    fun mostrar(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable seccionId: Long,
        @PathVariable unidadId: Long
    ): ResponseEntity<Any> {
        val id = idDe(jwt) ?: return sesionInvalida()
        if (!puedeGestionar(jwt, id, seccionId, unidadId)) return unidadNoEncontrada()
        repo.mostrarUnidad(seccionId, unidadId)
        return ResponseEntity.noContent().build()
    }

    private fun puedeGestionar(jwt: Jwt, usuarioId: Long, seccionId: Long, unidadId: Long): Boolean =
        repo.puedeGestionarUnidad(if (rolDe(jwt) == "ADMIN") null else usuarioId, seccionId, unidadId)

    private fun unidadNoEncontrada() = error(HttpStatus.NOT_FOUND, "Unidad o sección no encontrada")
}