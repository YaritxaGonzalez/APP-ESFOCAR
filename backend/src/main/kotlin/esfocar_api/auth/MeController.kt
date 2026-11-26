package esfocar_api.auth

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class SeccionResponse(val id: Long, val codigo: String, val nombre: String)
data class MeResponse(
    val id: Long,
    val alias: String,
    val rol: String,
    val creado: String,
    val seccion: SeccionResponse?
)

@RestController
@RequestMapping("/api")
class MeController(private val repo: UsuarioRepository) {

    // Cualquier usuario autenticado. El token solo identifica (sub = id);
    // los datos se leen de Oracle para que siempre estén al día.
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<Any> {
        val id = jwt.subject?.toLongOrNull() ?: return sesionInvalida()
        val perfil = repo.buscarPerfilPorId(id) ?: return sesionInvalida()

        val seccion = if (perfil.seccionId != null && perfil.seccionCodigo != null && perfil.seccionNombre != null)
            SeccionResponse(perfil.seccionId, perfil.seccionCodigo, perfil.seccionNombre)
        else null

        return ResponseEntity.ok(
            MeResponse(perfil.id, perfil.alias, perfil.rol, perfil.creado, seccion)
        )
    }

    // la cuenta ya no existe: el token deja de servir
    private fun sesionInvalida(): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Sesión no válida o expirada"))
}