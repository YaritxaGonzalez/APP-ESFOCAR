package esfocar_api.auth

import com.nimbusds.jose.jwk.source.ImmutableSecret
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
class ConfigSeguridad {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    // ---------- llave y codificación del JWT (HS256) ----------

    @Bean
    fun llaveJwt(@Value("\${app.jwt.secret}") secreto: String): SecretKey {
        val bytes = secreto.toByteArray(Charsets.UTF_8)
        require(bytes.size >= 32) { "JWT_SECRET debe tener al menos 32 caracteres" }
        return SecretKeySpec(bytes, "HmacSHA256")
    }

    @Bean
    fun jwtEncoder(llave: SecretKey): JwtEncoder = NimbusJwtEncoder(ImmutableSecret(llave))

    @Bean
    fun jwtDecoder(llave: SecretKey): JwtDecoder =
        NimbusJwtDecoder.withSecretKey(llave).macAlgorithm(MacAlgorithm.HS256).build()

    // el claim "rol" (ej. "ADMIN") pasa a la autoridad ROLE_ADMIN
    private fun convertidorJwt(): JwtAuthenticationConverter {
        val autoridades = JwtGrantedAuthoritiesConverter().apply {
            setAuthoritiesClaimName("rol")
            setAuthorityPrefix("ROLE_")
        }
        return JwtAuthenticationConverter().apply { setJwtGrantedAuthoritiesConverter(autoridades) }
    }

    // ---------- respuestas de error en JSON, mismo formato que AuthController ----------

    private fun responderJson(res: HttpServletResponse, estado: HttpStatus, mensaje: String) {
        res.status = estado.value()
        res.contentType = "application/json;charset=UTF-8"
        res.writer.write("""{"error":"$mensaje"}""")
    }

    private val sinSesion = AuthenticationEntryPoint { _, res, _ ->
        responderJson(res, HttpStatus.UNAUTHORIZED, "Sesión no válida o expirada")
    }

    private val sinPermiso = AccessDeniedHandler { _, res, _ ->
        responderJson(res, HttpStatus.FORBIDDEN, "No tienes permiso para esta acción")
    }

    // ---------- reglas de acceso ----------

    @Bean
    fun cadenaSeguridad(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }                       // API para app móvil, sin cookies
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { a ->
                a.requestMatchers("/error").permitAll()
                    .requestMatchers("/api/salud", "/api/auth/login", "/api/auth/registro").permitAll()
                    .requestMatchers("/api/salud/db", "/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/docente/**").hasAnyRole("DOCENTE", "ADMIN")
                    .requestMatchers("/api/estudiante/**").hasRole("ESTUDIANTE")
                    .anyRequest().authenticated()
            }
            .exceptionHandling { it.authenticationEntryPoint(sinSesion).accessDeniedHandler(sinPermiso) }
            .oauth2ResourceServer { rs ->
                rs.jwt { it.jwtAuthenticationConverter(convertidorJwt()) }
                rs.authenticationEntryPoint(sinSesion)
                rs.accessDeniedHandler(sinPermiso)
            }
        return http.build()
    }
}