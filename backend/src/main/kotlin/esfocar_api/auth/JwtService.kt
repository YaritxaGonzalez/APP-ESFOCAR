package esfocar_api.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

data class TokenEmitido(val token: String, val expiraEnSegundos: Long)

@Service
class JwtService(
    private val encoder: JwtEncoder,
    @Value("\${app.jwt.horas:8}") private val horas: Long
) {
    fun generar(usuario: Usuario): TokenEmitido {
        val ahora = Instant.now()
        val duracion = Duration.ofHours(horas)

        val claims = JwtClaimsSet.builder()
            .issuer("esfocar-api")
            .subject(usuario.id.toString())
            .issuedAt(ahora)
            .expiresAt(ahora.plus(duracion))
            .claim("alias", usuario.alias)
            .claim("rol", usuario.rol)
            .apply { usuario.seccionId?.let { claim("seccionId", it) } }
            .build()

        val cabecera = JwsHeader.with(MacAlgorithm.HS256).build()
        val token = encoder.encode(JwtEncoderParameters.from(cabecera, claims)).tokenValue
        return TokenEmitido(token, duracion.seconds)
    }
}