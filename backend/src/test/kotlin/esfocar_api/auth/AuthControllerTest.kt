package esfocar_api.auth

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthControllerTest {

    private val encoder = BCryptPasswordEncoder()
    private val repo = mockk<UsuarioRepository>()
    private val jwt = mockk<JwtService>()
    private val controller = AuthController(repo, encoder, jwt)

    private val usuario = Usuario(1, "est01", encoder.encode("Clave1234")!!, "ESTUDIANTE", 2)

    // ---------- login ----------

    @Test
    fun `login correcto devuelve 200 con token y usuario`() {
        every { repo.buscarPorAlias("est01") } returns usuario
        every { jwt.generar(usuario) } returns TokenEmitido("tok", 28800)

        val res = controller.login(LoginRequest("est01", "Clave1234"))

        assertEquals(HttpStatus.OK, res.statusCode)
        val cuerpo = assertIs<LoginResponse>(res.body)
        assertEquals("tok", cuerpo.token)
        assertEquals("Bearer", cuerpo.tipo)
        assertEquals("ESTUDIANTE", cuerpo.usuario.rol)
        assertEquals(2L, cuerpo.usuario.seccionId)
    }

    @Test
    fun `el alias se recorta antes de buscar`() {
        every { repo.buscarPorAlias("est01") } returns usuario
        every { jwt.generar(any()) } returns TokenEmitido("tok", 1)

        val res = controller.login(LoginRequest("  est01 ", "Clave1234"))

        assertEquals(HttpStatus.OK, res.statusCode)
    }

    @Test
    fun `contraseña incorrecta devuelve 401`() {
        every { repo.buscarPorAlias("est01") } returns usuario

        val res = controller.login(LoginRequest("est01", "otra"))

        assertEquals(HttpStatus.UNAUTHORIZED, res.statusCode)
        assertEquals(mapOf("error" to "Usuario o contraseña incorrectos"), res.body)
        verify(exactly = 0) { jwt.generar(any()) }
    }

    @Test
    fun `usuario inexistente devuelve el mismo 401 que contraseña mala`() {
        every { repo.buscarPorAlias("fantasma") } returns null

        val res = controller.login(LoginRequest("fantasma", "Clave1234"))

        assertEquals(HttpStatus.UNAUTHORIZED, res.statusCode)
        assertEquals(mapOf("error" to "Usuario o contraseña incorrectos"), res.body)
        verify(exactly = 0) { jwt.generar(any()) }
    }

    // ---------- registro ----------

    @Test
    fun `registro correcto crea un ESTUDIANTE y devuelve 201`() {
        every { repo.seccionIdPorCodigo("SEC-A-2026") } returns 2L
        every { repo.buscarPorAlias("nuevo01") } returns null
        every { repo.crear("nuevo01", any(), "ESTUDIANTE", 2L) } returns 9L

        val res = controller.registro(RegistroRequest("nuevo01", "Clave1234", "sec-a-2026"))

        assertEquals(HttpStatus.CREATED, res.statusCode)
        val cuerpo = assertIs<UsuarioResponse>(res.body)
        assertEquals(9L, cuerpo.id)
        assertEquals("ESTUDIANTE", cuerpo.rol)
    }

    @Test
    fun `registro con alias invalido devuelve 400`() {
        val res = controller.registro(RegistroRequest("ab", "Clave1234", "SEC-A-2026"))

        assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `registro con contraseña corta devuelve 400`() {
        val res = controller.registro(RegistroRequest("nuevo01", "corta", "SEC-A-2026"))

        assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
    }

    @Test
    fun `registro con codigo de seccion inexistente devuelve 400`() {
        every { repo.seccionIdPorCodigo("NOEXISTE") } returns null

        val res = controller.registro(RegistroRequest("nuevo01", "Clave1234", "noexiste"))

        assertEquals(HttpStatus.BAD_REQUEST, res.statusCode)
        assertEquals(mapOf("error" to "Código de sección no válido"), res.body)
    }

    @Test
    fun `registro con alias repetido devuelve 409`() {
        every { repo.seccionIdPorCodigo("SEC-A-2026") } returns 2L
        every { repo.buscarPorAlias("est01") } returns usuario

        val res = controller.registro(RegistroRequest("est01", "Clave1234", "SEC-A-2026"))

        assertEquals(HttpStatus.CONFLICT, res.statusCode)
        verify(exactly = 0) { repo.crear(any(), any(), any(), any()) }
    }
}
