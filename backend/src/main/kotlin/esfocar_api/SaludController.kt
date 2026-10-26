package esfocar_api

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class SaludController(private val jdbc: JdbcTemplate) {

    @GetMapping("/salud")
    fun salud() = mapOf("estado" to "ok")

    // temporal: solo para verificar la conexión a Oracle; se elimina más adelante
    @GetMapping("/salud/db")
    fun saludDb(): Map<String, Any?> = try {
        val asignaturas = jdbc.queryForObject("SELECT COUNT(*) FROM asignatura", Int::class.java)
        mapOf("estado" to "ok", "asignaturas" to asignaturas)
    } catch (e: Exception) {
        val causa = generateSequence<Throwable>(e) { it.cause }.last()
        mapOf("estado" to "error", "tipo" to causa.javaClass.simpleName, "mensaje" to causa.message?.take(300))
    }
}