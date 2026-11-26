package esfocar_api.auth

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementCreator
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Types

data class Usuario(
    val id: Long,
    val alias: String,
    val passwordHash: String,
    val rol: String,
    val seccionId: Long?
)

// datos del usuario con su sección ya resuelta (para /api/me)
data class PerfilUsuario(
    val id: Long,
    val alias: String,
    val rol: String,
    val creado: String,
    val seccionId: Long?,
    val seccionCodigo: String?,
    val seccionNombre: String?
)

@Repository
class UsuarioRepository(private val jdbc: JdbcTemplate) {

    private val mapper = RowMapper { rs, _ ->
        val seccion = rs.getLong("seccion_id")
        Usuario(
            id = rs.getLong("id"),
            alias = rs.getString("alias"),
            passwordHash = rs.getString("password_hash"),
            rol = rs.getString("rol"),
            seccionId = if (rs.wasNull()) null else seccion
        )
    }

    fun buscarPorAlias(alias: String): Usuario? =
        jdbc.query(
            "SELECT id, alias, password_hash, rol, seccion_id FROM usuario WHERE alias = ?",
            mapper, alias
        ).firstOrNull()

    fun buscarPerfilPorId(id: Long): PerfilUsuario? =
        jdbc.query(
            """
            SELECT u.id, u.alias, u.rol, u.creado,
                   s.id AS sec_id, s.codigo AS sec_codigo, s.nombre AS sec_nombre
            FROM usuario u
            LEFT JOIN seccion s ON s.id = u.seccion_id
            WHERE u.id = ?
            """.trimIndent(),
            RowMapper { rs, _ ->
                val secId = rs.getLong("sec_id")
                val sinSeccion = rs.wasNull()
                PerfilUsuario(
                    id = rs.getLong("id"),
                    alias = rs.getString("alias"),
                    rol = rs.getString("rol"),
                    creado = rs.getTimestamp("creado").toInstant().toString(),
                    seccionId = if (sinSeccion) null else secId,
                    seccionCodigo = rs.getString("sec_codigo"),
                    seccionNombre = rs.getString("sec_nombre")
                )
            },
            id
        ).firstOrNull()

    fun seccionIdPorCodigo(codigo: String): Long? =
        jdbc.query(
            "SELECT id FROM seccion WHERE codigo = ?",
            RowMapper { rs, _ -> rs.getLong("id") }, codigo
        ).firstOrNull()

    fun crear(alias: String, passwordHash: String, rol: String, seccionId: Long?): Long {
        val llave = GeneratedKeyHolder()
        jdbc.update(PreparedStatementCreator { con ->
            con.prepareStatement(
                "INSERT INTO usuario (alias, password_hash, rol, seccion_id) VALUES (?, ?, ?, ?)",
                arrayOf("ID")
            ).apply {
                setString(1, alias)
                setString(2, passwordHash)
                setString(3, rol)
                if (seccionId == null) setNull(4, Types.NUMERIC) else setLong(4, seccionId)
            }
        }, llave)
        return (llave.key as Number).toLong()
    }

    // asigna un docente a una sección; asignaturaId = null significa "todas las asignaturas".
    // No duplica asignaciones existentes. Devuelve cuántas filas nuevas creó.
    fun asignarDocente(docenteId: Long, seccionId: Long, asignaturaId: Long?): Int {
        val filtro = if (asignaturaId != null) "AND a.id = ?" else ""
        val sql = """
            INSERT INTO docente_seccion (docente_id, seccion_id, asignatura_id)
            SELECT ?, ?, a.id FROM asignatura a
            WHERE NOT EXISTS (SELECT 1 FROM docente_seccion x
                              WHERE x.docente_id = ? AND x.seccion_id = ? AND x.asignatura_id = a.id)
            $filtro
        """.trimIndent()
        val args = mutableListOf<Any>(docenteId, seccionId, docenteId, seccionId)
        if (asignaturaId != null) args += asignaturaId
        return jdbc.update(sql, *args.toTypedArray())
    }

    // asigna un docente a la sección A en todas las asignaturas (solo para el MVP)
    fun asignarDocenteASeccionA(docenteId: Long) {
        jdbc.update(
            """
            INSERT INTO docente_seccion (docente_id, seccion_id, asignatura_id)
            SELECT ?, s.id, a.id FROM seccion s CROSS JOIN asignatura a
            WHERE s.codigo = 'SEC-A-2026'
            """.trimIndent(),
            docenteId
        )
    }
}