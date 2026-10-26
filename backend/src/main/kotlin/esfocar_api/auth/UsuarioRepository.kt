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