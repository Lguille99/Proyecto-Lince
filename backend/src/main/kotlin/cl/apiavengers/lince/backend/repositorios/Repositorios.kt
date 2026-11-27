package cl.apiavengers.lince.backend.repositorios

import cl.apiavengers.lince.backend.db.Db
import cl.apiavengers.lince.backend.db.actualizar
import cl.apiavengers.lince.backend.db.consultar
import cl.apiavengers.lince.backend.db.consultarUno
import cl.apiavengers.lince.backend.modelo.DisponibilidadDto
import cl.apiavengers.lince.backend.modelo.UsuarioDto
import cl.apiavengers.lince.backend.modelo.GeocercaDto
import cl.apiavengers.lince.backend.modelo.HitoDto
import cl.apiavengers.lince.backend.modelo.IncidenciaDto
import cl.apiavengers.lince.backend.modelo.UltimaPosicionDto
import cl.apiavengers.lince.backend.seguridad.RolApi
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

data class UsuarioFila(
    val id: String,
    val email: String,
    val nombre: String,
    val rol: RolApi,
    val passwordHash: String
) {
    fun principal() = cl.apiavengers.lince.backend.seguridad.UsuarioPrincipal(id, email, nombre, rol)
}

data class ServicioRef(
    val id: String,
    val tipo: String,
    val titulo: String,
    val fechaHoraPickup: String,
    val estado: String
)

object UsuariosRepo {
    fun porEmail(email: String): UsuarioFila? = Db.usar { conexion ->
        conexion.consultarUno(
            "SELECT id, email, nombre, rol, password_hash FROM usuarios WHERE email = ? AND activo = 1",
            email.lowercase()
        ) { fila ->
            UsuarioFila(
                id = fila.getString("id"),
                email = fila.getString("email"),
                nombre = fila.getString("nombre"),
                rol = RolApi.valueOf(fila.getString("rol")),
                passwordHash = fila.getString("password_hash")
            )
        }
    }

    fun porId(id: String): UsuarioFila? = Db.usar { conexion ->
        conexion.consultarUno(
            "SELECT id, email, nombre, rol, password_hash FROM usuarios WHERE id = ? AND activo = 1",
            id
        ) { fila ->
            UsuarioFila(
                id = fila.getString("id"),
                email = fila.getString("email"),
                nombre = fila.getString("nombre"),
                rol = RolApi.valueOf(fila.getString("rol")),
                passwordHash = fila.getString("password_hash")
            )
        }
    }

    fun listar(): List<UsuarioFila> = Db.usar { conexion ->
        conexion.consultar(
            "SELECT id, email, nombre, rol, password_hash FROM usuarios WHERE activo = 1 ORDER BY id"
        ) { fila ->
            UsuarioFila(
                id = fila.getString("id"),
                email = fila.getString("email"),
                nombre = fila.getString("nombre"),
                rol = RolApi.valueOf(fila.getString("rol")),
                passwordHash = fila.getString("password_hash")
            )
        }
    }

    fun crear(email: String, nombre: String, rol: RolApi, password: String): UsuarioFila {
        val id = "USR-" + java.util.UUID.randomUUID().toString().take(8).uppercase()
        val hash = cl.apiavengers.lince.backend.seguridad.Password.hash(password)
        Db.usar { conexion ->
            conexion.actualizar(
                "INSERT INTO usuarios (id, email, nombre, rol, password_hash, activo) VALUES (?, ?, ?, ?, ?, 1)",
                id, email.lowercase(), nombre, rol.name, hash
            )
        }
        return porId(id)!!
    }

    fun actualizar(id: String, email: String?, nombre: String?, rol: RolApi?, password: String?): UsuarioFila? {
        val actual = porId(id) ?: return null
        val nuevoEmail = email?.lowercase() ?: actual.email
        val nuevoNombre = nombre ?: actual.nombre
        val nuevoRol = rol ?: actual.rol
        val nuevoHash = password?.let { cl.apiavengers.lince.backend.seguridad.Password.hash(it) } ?: actual.passwordHash
        Db.usar { conexion ->
            conexion.actualizar(
                "UPDATE usuarios SET email = ?, nombre = ?, rol = ?, password_hash = ? WHERE id = ?",
                nuevoEmail, nuevoNombre, nuevoRol.name, nuevoHash, id
            )
        }
        return porId(id)
    }

    fun borrar(id: String): Boolean = Db.usar { conexion ->
        conexion.actualizar("UPDATE usuarios SET activo = 0 WHERE id = ?", id) > 0
    }
}

object ServiciosRepo {
    fun buscar(id: String): ServicioRef? = Db.usar { conexion ->
        conexion.consultarUno(
            "SELECT id, tipo, titulo, fecha_hora_pickup, estado FROM servicios_referencia WHERE id = ?",
            id
        ) { fila ->
            ServicioRef(
                id = fila.getString("id"),
                tipo = fila.getString("tipo"),
                titulo = fila.getString("titulo"),
                fechaHoraPickup = fila.getString("fecha_hora_pickup"),
                estado = fila.getString("estado")
            )
        }
    }

    fun tieneAsignacion(usuarioId: String, fecha: String): Boolean = Db.usar { conexion ->
        conexion.consultar(
            "SELECT 1 FROM asignaciones_referencia WHERE usuario_id = ? AND fecha = ? LIMIT 1",
            usuarioId, fecha
        ) { true }.isNotEmpty()
    }

    fun estaAsignado(servicioId: String, usuarioId: String): Boolean = Db.usar { conexion ->
        conexion.consultar(
            "SELECT 1 FROM asignaciones_referencia WHERE servicio_id = ? AND usuario_id = ? LIMIT 1",
            servicioId, usuarioId
        ) { true }.isNotEmpty()
    }
}

object GeocercasRepo {
    private fun fila(rs: ResultSet) = GeocercaDto(
        id = rs.getString("id"),
        servicioId = rs.getString("servicio_id"),
        tipo = rs.getString("tipo"),
        nombre = rs.getString("nombre"),
        lat = rs.getDouble("lat"),
        lng = rs.getDouble("lng"),
        radioM = rs.getInt("radio_m"),
        creadoPor = rs.getString("creado_por"),
        creadoEn = rs.getString("creado_en"),
        actualizadoEn = rs.getString("actualizado_en")
    )

    fun listar(servicioId: String?): List<GeocercaDto> = Db.usar { conexion ->
        if (servicioId == null) conexion.consultar("SELECT * FROM geocercas ORDER BY creado_en") { fila(it) }
        else conexion.consultar(
            "SELECT * FROM geocercas WHERE servicio_id = ? ORDER BY tipo", servicioId
        ) { fila(it) }
    }

    fun buscar(id: String): GeocercaDto? = Db.usar { conexion ->
        conexion.consultarUno("SELECT * FROM geocercas WHERE id = ?", id) { fila(it) }
    }

    fun paraServicio(servicioId: String, geocercaId: String?): GeocercaDto? = Db.usar { conexion ->
        if (geocercaId != null) {
            conexion.consultarUno("SELECT * FROM geocercas WHERE id = ?", geocercaId) { fila(it) }
        } else {
            conexion.consultarUno(
                "SELECT * FROM geocercas WHERE servicio_id = ? " +
                    "ORDER BY CASE tipo WHEN 'PICKUP' THEN 0 ELSE 1 END LIMIT 1",
                servicioId
            ) { fila(it) }
        }
    }

    fun crear(id: String, request: cl.apiavengers.lince.backend.modelo.GeocercaRequest, usuarioId: String): GeocercaDto {
        val ahora = Instant.now().toString()
        Db.usar { conexion ->
            conexion.actualizar(
                "INSERT INTO geocercas (id, servicio_id, tipo, nombre, lat, lng, radio_m, creado_por, " +
                    "creado_en, actualizado_en) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, request.servicioId, request.tipo.uppercase(), request.nombre.trim(),
                request.lat, request.lng, request.radioM, usuarioId, ahora, ahora
            )
        }
        return buscar(id)!!
    }

    fun actualizar(id: String, request: cl.apiavengers.lince.backend.modelo.GeocercaRequest): GeocercaDto? {
        Db.usar { conexion ->
            conexion.actualizar(
                "UPDATE geocercas SET servicio_id = ?, tipo = ?, nombre = ?, lat = ?, lng = ?, " +
                    "radio_m = ?, actualizado_en = ? WHERE id = ?",
                request.servicioId, request.tipo.uppercase(), request.nombre.trim(), request.lat,
                request.lng, request.radioM, Instant.now().toString(), id
            )
        }
        return buscar(id)
    }

    fun borrar(id: String): Boolean = Db.usar { conexion ->
        conexion.actualizar("DELETE FROM geocercas WHERE id = ?", id) > 0
    }
}

object HitosRepo {
    private fun fila(rs: ResultSet) = HitoDto(
        id = rs.getString("id"),
        servicioId = rs.getString("servicio_id"),
        geocercaId = rs.getString("geocerca_id"),
        usuarioId = rs.getString("usuario_id"),
        tipo = rs.getString("tipo"),
        lat = rs.getDouble("lat").takeUnless { rs.wasNull() },
        lng = rs.getDouble("lng").takeUnless { rs.wasNull() },
        precisionM = rs.getDouble("precision_m").takeUnless { rs.wasNull() },
        distanciaM = rs.getDouble("distancia_m").takeUnless { rs.wasNull() },
        ocurridoEn = rs.getString("ocurrido_en"),
        observaciones = rs.getString("observaciones"),
        motivoRechazo = rs.getString("motivo_rechazo"),
        requestId = rs.getString("request_id"),
        estadoSync = rs.getString("estado_sync")
    )

    fun listar(servicioId: String?, tipo: String?, usuarioId: String?): List<HitoDto> = Db.usar { conexion ->
        val condiciones = mutableListOf<String>()
        val parametros = mutableListOf<Any?>()
        servicioId?.let { condiciones += "servicio_id = ?"; parametros += it }
        tipo?.let { condiciones += "tipo = ?"; parametros += it.uppercase() }
        usuarioId?.let { condiciones += "usuario_id = ?"; parametros += it }
        val filtro = if (condiciones.isEmpty()) "" else "WHERE " + condiciones.joinToString(" AND ")
        conexion.consultar("SELECT * FROM hitos $filtro ORDER BY ocurrido_en DESC", *parametros.toTypedArray()) {
            fila(it)
        }
    }

    fun buscar(id: String): HitoDto? = Db.usar { conexion ->
        conexion.consultarUno("SELECT * FROM hitos WHERE id = ?", id) { fila(it) }
    }

    fun crear(
        id: String,
        servicioId: String,
        geocercaId: String?,
        usuarioId: String,
        tipo: String,
        lat: Double?,
        lng: Double?,
        precisionM: Double?,
        distanciaM: Double?,
        ocurridoEn: String,
        observaciones: String?,
        motivoRechazo: String?,
        requestId: String?
    ): HitoDto {
        Db.usar { conexion ->
            conexion.actualizar(
                "INSERT INTO hitos (id, servicio_id, geocerca_id, usuario_id, tipo, lat, lng, precision_m, " +
                    "distancia_m, ocurrido_en, observaciones, motivo_rechazo, request_id, estado_sync, creado_en) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SYNCED', ?)",
                id, servicioId, geocercaId, usuarioId, tipo.uppercase(), lat, lng, precisionM,
                distanciaM, ocurridoEn, observaciones, motivoRechazo, requestId, Instant.now().toString()
            )
        }
        return buscar(id)!!
    }

    fun actualizar(
        id: String,
        observaciones: String?,
        motivoRechazo: String?
    ): HitoDto? {
        Db.usar { conexion ->
            conexion.actualizar(
                "UPDATE hitos SET observaciones = ?, motivo_rechazo = ? WHERE id = ?",
                observaciones, motivoRechazo, id
            )
        }
        return buscar(id)
    }

    fun borrar(id: String): Boolean = Db.usar { conexion ->
        conexion.actualizar("DELETE FROM hitos WHERE id = ?", id) > 0
    }

    fun existePorRequestId(servicioId: String, requestId: String): Boolean = Db.usar { conexion ->
        conexion.consultar(
            "SELECT 1 FROM hitos WHERE servicio_id = ? AND request_id = ? LIMIT 1",
            servicioId, requestId
        ) { true }.isNotEmpty()
    }

    fun buscarPorRequestId(servicioId: String, requestId: String): HitoDto? = Db.usar { conexion ->
        conexion.consultarUno(
            "SELECT * FROM hitos WHERE servicio_id = ? AND request_id = ?",
            servicioId, requestId
        ) { fila ->
            fila(
                id = fila.getString("id"),
                servicioId = fila.getString("servicio_id"),
                geocercaId = fila.getString("geocerca_id"),
                usuarioId = fila.getString("usuario_id"),
                tipo = fila.getString("tipo"),
                lat = fila.getDouble("lat").takeUnless { fila.wasNull() },
                lng = fila.getDouble("lng").takeUnless { fila.wasNull() },
                precisionM = fila.getDouble("precision_m").takeUnless { fila.wasNull() },
                distanciaM = fila.getDouble("distancia_m").takeUnless { fila.wasNull() },
                ocurridoEn = fila.getString("ocurrido_en"),
                observaciones = fila.getString("observaciones"),
                motivoRechazo = fila.getString("motivo_rechazo"),
                requestId = fila.getString("request_id"),
                estadoSync = fila.getString("estado_sync")
            )
        }
    }

    fun ultimaPosicion(servicioId: String): UltimaPosicionDto? = Db.usar { conexion ->
        val ultimo = conexion.consultarUno(
            "SELECT tipo FROM hitos WHERE servicio_id = ? ORDER BY ocurrido_en DESC LIMIT 1",
            servicioId
        ) { it.getString("tipo") }
        if (ultimo == null || ultimo == "CHECK_OUT") return@usar null

        conexion.consultarUno(
            "SELECT * FROM hitos WHERE servicio_id = ? AND lat IS NOT NULL AND lng IS NOT NULL " +
                "AND tipo IN ('CHECK_IN', 'REPORTE_POSICION') ORDER BY ocurrido_en DESC LIMIT 1",
            servicioId
        ) { fila ->
            UltimaPosicionDto(
                servicioId = servicioId,
                lat = fila.getDouble("lat"),
                lng = fila.getDouble("lng"),
                precisionM = fila.getDouble("precision_m").takeUnless { fila.wasNull() },
                reportadoEn = fila.getString("ocurrido_en"),
                tipoEvento = fila.getString("tipo"),
                usuarioId = fila.getString("usuario_id")
            )
        }
    }
}

object DisponibilidadRepo {
    private fun fila(rs: ResultSet) = DisponibilidadDto(
        id = rs.getString("id"),
        usuarioId = rs.getString("usuario_id"),
        fecha = rs.getString("fecha"),
        motivo = rs.getString("motivo"),
        creadoEn = rs.getString("creado_en"),
        estado = rs.getString("estado")
    )

    fun listar(usuarioId: String?): List<DisponibilidadDto> = Db.usar { conexion ->
        if (usuarioId == null) {
            conexion.consultar("SELECT * FROM disponibilidad ORDER BY fecha") { fila(it) }
        } else {
            conexion.consultar(
                "SELECT * FROM disponibilidad WHERE usuario_id = ? ORDER BY fecha", usuarioId
            ) { fila(it) }
        }
    }

    fun buscar(id: String): DisponibilidadDto? = Db.usar { conexion ->
        conexion.consultarUno("SELECT * FROM disponibilidad WHERE id = ?", id) { fila(it) }
    }

    fun existe(usuarioId: String, fecha: String): Boolean = Db.usar { conexion ->
        conexion.consultar(
            "SELECT 1 FROM disponibilidad WHERE usuario_id = ? AND fecha = ? LIMIT 1",
            usuarioId, fecha
        ) { true }.isNotEmpty()
    }

    fun crear(id: String, usuarioId: String, fecha: String, motivo: String?): DisponibilidadDto {
        Db.usar { conexion ->
            conexion.actualizar(
                "INSERT INTO disponibilidad (id, usuario_id, fecha, motivo, creado_en, estado) " +
                    "VALUES (?, ?, ?, ?, ?, 'ACTIVO')",
                id, usuarioId, fecha, motivo, Instant.now().toString()
            )
        }
        return buscar(id)!!
    }

    fun borrar(id: String): Boolean = Db.usar { conexion ->
        conexion.actualizar("DELETE FROM disponibilidad WHERE id = ?", id) > 0
    }
}

object IncidenciasRepo {
    private fun fila(rs: ResultSet) = IncidenciaDto(
        id = rs.getString("id"),
        servicioId = rs.getString("servicio_id"),
        usuarioId = rs.getString("usuario_id"),
        categoria = rs.getString("categoria"),
        descripcion = rs.getString("descripcion"),
        lat = rs.getDouble("lat").takeUnless { rs.wasNull() },
        lng = rs.getDouble("lng").takeUnless { rs.wasNull() },
        ocurridoEn = rs.getString("ocurrido_en"),
        creadoEn = rs.getString("creado_en"),
        estado = rs.getString("estado")
    )

    fun listar(servicioId: String?, usuarioId: String?): List<IncidenciaDto> = Db.usar { conexion ->
        val condiciones = mutableListOf<String>()
        val parametros = mutableListOf<Any?>()
        servicioId?.let { condiciones += "servicio_id = ?"; parametros += it }
        usuarioId?.let { condiciones += "usuario_id = ?"; parametros += it }
        val filtro = if (condiciones.isEmpty()) "" else "WHERE " + condiciones.joinToString(" AND ")
        conexion.consultar(
            "SELECT * FROM incidencias $filtro ORDER BY ocurrido_en DESC",
            *parametros.toTypedArray()
        ) { fila(it) }
    }

    fun buscar(id: String): IncidenciaDto? = Db.usar { conexion ->
        conexion.consultarUno("SELECT * FROM incidencias WHERE id = ?", id) { fila(it) }
    }

    fun crear(
        id: String,
        request: cl.apiavengers.lince.backend.modelo.IncidenciaRequest,
        usuarioId: String,
        ocurridoEn: String
    ): IncidenciaDto {
        Db.usar { conexion ->
            conexion.actualizar(
                "INSERT INTO incidencias (id, servicio_id, usuario_id, categoria, descripcion, lat, lng, " +
                    "ocurrido_en, creado_en, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ABIERTA')",
                id, request.servicioId, usuarioId, request.categoria.trim(),
                request.descripcion.trim(), request.lat, request.lng, ocurridoEn, Instant.now().toString()
            )
        }
        return buscar(id)!!
    }

    fun actualizar(id: String, estado: String): IncidenciaDto? {
        Db.usar { conexion ->
            conexion.actualizar("UPDATE incidencias SET estado = ? WHERE id = ?", estado.uppercase(), id)
        }
        return buscar(id)
    }

    fun borrar(id: String): Boolean = Db.usar { conexion ->
        conexion.actualizar("DELETE FROM incidencias WHERE id = ?", id) > 0
    }
}

fun nuevoId(): String = UUID.randomUUID().toString()

fun cl.apiavengers.lince.backend.repositorios.UsuarioFila.aDtoUsuario() =
    cl.apiavengers.lince.backend.modelo.UsuarioDto(id, email, nombre, rol.name)
