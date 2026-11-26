# ESFOCAR API (Spring Boot + Kotlin + Oracle)

## Variables de entorno (obligatorias)
| Variable | Para qué |
|---|---|
| `DB_URL` | URL JDBC de Oracle (con `TNS_ADMIN` apuntando a la wallet) |
| `DB_USER` / `DB_PASSWORD` | Credenciales de Oracle |
| `JWT_SECRET` | Secreto del token, mínimo 32 caracteres |
| `PORT` | Opcional (por defecto 8081) |

Cuentas iniciales (opcional): copiar `application-local.properties.example` a `application-local.properties`.

## Ejecutar
```
./gradlew bootRun      # servidor en http://localhost:8081
./gradlew test         # pruebas unitarias
./gradlew bootJar      # JAR para desplegar (Dockerfile incluido)
```
Desde el emulador Android la URL es `http://10.0.2.2:8081`.

## Endpoints
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/salud` | público | Estado del servicio |
| POST | `/api/auth/registro` | público | Crea un ESTUDIANTE (`alias`, `password`, `codigoSeccion`) |
| POST | `/api/auth/login` | público | Devuelve token JWT `Bearer` |
| GET | `/api/me` | autenticado | Perfil y sección del usuario |
| GET | `/api/asignaturas` | autenticado | Asignaturas según rol |
| GET | `/api/asignaturas/{id}/unidades` | autenticado | Unidades según rol |
| PUT/DELETE | `/api/docente/secciones/{s}/unidades/{u}/oculta` | DOCENTE, ADMIN | Ocultar / mostrar unidad |
| POST | `/api/admin/usuarios` | ADMIN | Crea ESTUDIANTE o DOCENTE |
| POST | `/api/admin/asignaciones` | ADMIN | Asigna docente a sección/asignatura |
| GET | `/api/salud/db` | ADMIN | Prueba de conexión a Oracle |

Las peticiones protegidas llevan el header `Authorization: Bearer <token>`.

## Arquitectura
Controller → Repository (`JdbcTemplate`) → Oracle. Seguridad stateless con JWT (HS256), contraseñas con BCrypt, roles `ESTUDIANTE`, `DOCENTE`, `ADMIN`.

## Tablas Oracle requeridas
`usuario` (con columna `creado`), `seccion`, `asignatura`, `unidad`, `docente_seccion`, `unidad_oculta`.
