package esfocar_api.auth

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.spec.SecretKeySpec
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JwtServiceTest {

    private val llave = SecretKeySpec("clave-de-prueba-de-al-menos-32-caracteres".toByteArray(), "HmacSHA256")
    private val servicio = JwtService(NimbusJwtEncoder(ImmutableSecret(llave)), horas = 8)
    private val decoder = NimbusJwtDecoder.withSecretKey(llave).macAlgorithm(MacAlgorithm.HS256).build()

    private fun usuario(seccionId: Long? = 3) = Usuario(7, "est01", "hash", "ESTUDIANTE", seccionId)

    @Test
    fun `el token trae id, alias y rol`() {
        val jwt = decoder.decode(servicio.generar(usuario()).token)

        assertEquals("7", jwt.subject)
        assertEquals("est01", jwt.getClaimAsString("alias"))
        assertEquals("ESTUDIANTE", jwt.getClaimAsString("rol"))
        assertEquals("esfocar-api", jwt.getClaimAsString("iss"))
    }

    @Test
    fun `incluye seccionId solo cuando existe`() {
        val con = decoder.decode(servicio.generar(usuario(3)).token)
        val sin = decoder.decode(servicio.generar(usuario(null)).token)

        assertEquals(3L, (con.claims["seccionId"] as Number).toLong())
        assertFalse(sin.claims.containsKey("seccionId"))
    }

    @Test
    fun `expira en las horas configuradas`() {
        val emitido = servicio.generar(usuario())
        val jwt = decoder.decode(emitido.token)

        assertEquals(8 * 3600L, emitido.expiraEnSegundos)
        assertEquals(emitido.expiraEnSegundos, jwt.expiresAt!!.epochSecond - jwt.issuedAt!!.epochSecond)
    }

    @Test
    fun `un token firmado con otra llave es rechazado`() {
        val otraLlave = SecretKeySpec("otra-clave-distinta-de-32-caracteres!!".toByteArray(), "HmacSHA256")
        val ajeno = JwtService(NimbusJwtEncoder(ImmutableSecret(otraLlave)), 8).generar(usuario()).token

        assertFailsWith<BadJwtException> { decoder.decode(ajeno) }
        assertTrue(servicio.generar(usuario()).token.isNotBlank())
    }
}