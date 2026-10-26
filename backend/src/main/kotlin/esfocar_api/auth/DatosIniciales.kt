package esfocar_api.auth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

// Crea las cuentas ficticias de mesa central y docente al arrancar, si no existen.
// Las claves vienen de application-local.properties, nunca del código.
@Component
class DatosIniciales(
    private val repo: UsuarioRepository,
    private val encoder: PasswordEncoder,
    @Value("\${app.seed.admin-password:}") private val claveAdmin: String,
    @Value("\${app.seed.docente-password:}") private val claveDocente: String
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(DatosIniciales::class.java)

    override fun run(args: ApplicationArguments) {
        // si Oracle no responde al arrancar, el servidor igual inicia y lo avisa
        try {
            sembrar("admin01", claveAdmin, "ADMIN")
            sembrar("docente01", claveDocente, "DOCENTE")
        } catch (e: Exception) {
            log.warn("No se pudieron sembrar las cuentas iniciales: {}", e.javaClass.simpleName)
        }
    }

    private fun sembrar(alias: String, clave: String, rol: String) {
        if (repo.buscarPorAlias(alias) != null) return
        if (clave.length < 8) {
            log.warn("Falta app.seed para {}: la cuenta no se creó", alias)
            return
        }
        val id = repo.crear(alias, encoder.encode(clave)!!, rol, null)
        if (rol == "DOCENTE") repo.asignarDocenteASeccionA(id)
        log.info("Cuenta {} creada", alias)
    }
}