package esfocar_api.catalogo

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

data class SeccionResumen(val id: Long, val codigo: String, val nombre: String)

// secciones: solo se llena para DOCENTE (dónde dicta la asignatura); en los demás roles va null
data class AsignaturaResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val secciones: List<SeccionResumen>? = null
)

// ocultaEn: solo se llena para DOCENTE y ADMIN; el estudiante nunca recibe unidades ocultas
data class UnidadResponse(
    val id: Long,
    val orden: Int,
    val titulo: String,
    val descripcion: String?,
    val ocultaEn: List<SeccionResumen>? = null
)

private data class FilaDocente(val id: Long, val nombre: String, val descripcion: String?, val seccion: SeccionResumen)

@Repository
class CatalogoRepository(private val jdbc: JdbcTemplate) {

    private fun contar(sql: String, vararg args: Any): Boolean =
        (jdbc.queryForObject(sql, Int::class.java, *args) ?: 0) > 0

    private val mapperAsignatura = RowMapper { rs, _ ->
        AsignaturaResponse(rs.getLong("id"), rs.getString("nombre"), rs.getString("descripcion"))
    }

    // ---------- usuario ----------

    fun seccionDeUsuario(usuarioId: Long): Long? =
        jdbc.query(
            "SELECT seccion_id FROM usuario WHERE id = ? AND seccion_id IS NOT NULL",
            RowMapper { rs, _ -> rs.getLong("seccion_id") }, usuarioId
        ).firstOrNull()

    // ---------- asignaturas ----------

    fun todasLasAsignaturas(): List<AsignaturaResponse> =
        jdbc.query("SELECT id, nombre, descripcion FROM asignatura ORDER BY id", mapperAsignatura)

    // el estudiante ve las asignaturas que se dictan en su sección
    fun asignaturasDeSeccion(seccionId: Long): List<AsignaturaResponse> =
        jdbc.query(
            """
            SELECT DISTINCT a.id, a.nombre, a.descripcion
            FROM asignatura a
            JOIN docente_seccion ds ON ds.asignatura_id = a.id
            WHERE ds.seccion_id = ?
            ORDER BY a.id
            """.trimIndent(),
            mapperAsignatura, seccionId
        )

    fun asignaturasDeDocente(docenteId: Long): List<AsignaturaResponse> {
        val filas = jdbc.query(
            """
            SELECT a.id, a.nombre, a.descripcion,
                   s.id AS sec_id, s.codigo AS sec_codigo, s.nombre AS sec_nombre
            FROM docente_seccion ds
            JOIN asignatura a ON a.id = ds.asignatura_id
            JOIN seccion s ON s.id = ds.seccion_id
            WHERE ds.docente_id = ?
            ORDER BY a.id, s.codigo
            """.trimIndent(),
            RowMapper { rs, _ ->
                FilaDocente(
                    rs.getLong("id"), rs.getString("nombre"), rs.getString("descripcion"),
                    SeccionResumen(rs.getLong("sec_id"), rs.getString("sec_codigo"), rs.getString("sec_nombre"))
                )
            },
            docenteId
        )
        return filas.groupBy { it.id }.map { (id, fs) ->
            AsignaturaResponse(id, fs[0].nombre, fs[0].descripcion, fs.map { it.seccion })
        }
    }

    // ---------- ¿puede este usuario ver la asignatura? (si no, el controlador responde 404) ----------

    fun asignaturaExiste(asignaturaId: Long): Boolean =
        contar("SELECT COUNT(*) FROM asignatura WHERE id = ?", asignaturaId)

    fun seccionDictaAsignatura(seccionId: Long, asignaturaId: Long): Boolean =
        contar("SELECT COUNT(*) FROM docente_seccion WHERE seccion_id = ? AND asignatura_id = ?", seccionId, asignaturaId)

    fun docenteDictaAsignatura(docenteId: Long, asignaturaId: Long): Boolean =
        contar("SELECT COUNT(*) FROM docente_seccion WHERE docente_id = ? AND asignatura_id = ?", docenteId, asignaturaId)

    // ---------- unidades ----------

    // estudiante: sin las unidades ocultas para su sección
    fun unidadesVisiblesParaSeccion(asignaturaId: Long, seccionId: Long): List<UnidadResponse> =
        jdbc.query(
            """
            SELECT u.id, u.orden, u.titulo, u.descripcion
            FROM unidad u
            WHERE u.asignatura_id = ?
              AND NOT EXISTS (SELECT 1 FROM unidad_oculta uo
                              WHERE uo.unidad_id = u.id AND uo.seccion_id = ?)
            ORDER BY u.orden
            """.trimIndent(),
            RowMapper { rs, _ ->
                UnidadResponse(rs.getLong("id"), rs.getInt("orden"), rs.getString("titulo"), rs.getString("descripcion"))
            },
            asignaturaId, seccionId
        )

    // docente (docenteId) o admin (docenteId = null): todas las unidades, con las secciones donde están ocultas.
    // Al docente solo se le muestran las secciones donde él dicta esa asignatura.
    fun unidadesParaGestion(asignaturaId: Long, docenteId: Long?): List<UnidadResponse> {
        val base = jdbc.query(
            "SELECT id, orden, titulo, descripcion FROM unidad WHERE asignatura_id = ? ORDER BY orden",
            RowMapper { rs, _ ->
                UnidadResponse(rs.getLong("id"), rs.getInt("orden"), rs.getString("titulo"), rs.getString("descripcion"))
            },
            asignaturaId
        )

        val sqlOcultas = StringBuilder(
            """
            SELECT uo.unidad_id, s.id AS sec_id, s.codigo AS sec_codigo, s.nombre AS sec_nombre
            FROM unidad_oculta uo
            JOIN unidad u ON u.id = uo.unidad_id
            JOIN seccion s ON s.id = uo.seccion_id
            WHERE u.asignatura_id = ?
            """.trimIndent()
        )
        val args = mutableListOf<Any>(asignaturaId)
        if (docenteId != null) {
            sqlOcultas.append(
                " AND uo.seccion_id IN (SELECT seccion_id FROM docente_seccion WHERE docente_id = ? AND asignatura_id = ?)"
            )
            args += docenteId
            args += asignaturaId
        }
        sqlOcultas.append(" ORDER BY s.codigo")

        val ocultas = jdbc.query(
            sqlOcultas.toString(),
            RowMapper { rs, _ ->
                rs.getLong("unidad_id") to
                        SeccionResumen(rs.getLong("sec_id"), rs.getString("sec_codigo"), rs.getString("sec_nombre"))
            },
            *args.toTypedArray()
        ).groupBy({ it.first }, { it.second })

        return base.map { it.copy(ocultaEn = ocultas[it.id] ?: emptyList()) }
    }

    // ---------- ocultar / mostrar ----------

    // docente: debe dictar la asignatura de esa unidad en esa sección. admin (docenteId = null): basta que la sección la tenga.
    fun puedeGestionarUnidad(docenteId: Long?, seccionId: Long, unidadId: Long): Boolean =
        if (docenteId != null)
            contar(
                """
                SELECT COUNT(*) FROM docente_seccion ds
                JOIN unidad u ON u.asignatura_id = ds.asignatura_id
                WHERE ds.docente_id = ? AND ds.seccion_id = ? AND u.id = ?
                """.trimIndent(),
                docenteId, seccionId, unidadId
            )
        else
            contar(
                """
                SELECT COUNT(*) FROM docente_seccion ds
                JOIN unidad u ON u.asignatura_id = ds.asignatura_id
                WHERE ds.seccion_id = ? AND u.id = ?
                """.trimIndent(),
                seccionId, unidadId
            )

    // idempotente: si ya estaba oculta, no hace nada
    fun ocultarUnidad(seccionId: Long, unidadId: Long, usuarioId: Long) {
        jdbc.update(
            """
            INSERT INTO unidad_oculta (seccion_id, unidad_id, docente_id)
            SELECT ?, ?, ? FROM dual
            WHERE NOT EXISTS (SELECT 1 FROM unidad_oculta WHERE seccion_id = ? AND unidad_id = ?)
            """.trimIndent(),
            seccionId, unidadId, usuarioId, seccionId, unidadId
        )
    }

    fun mostrarUnidad(seccionId: Long, unidadId: Long) {
        jdbc.update("DELETE FROM unidad_oculta WHERE seccion_id = ? AND unidad_id = ?", seccionId, unidadId)
    }
}