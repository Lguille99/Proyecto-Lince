package cl.apiavengers.lince.backend.db

import cl.apiavengers.lince.backend.Config
import cl.apiavengers.lince.backend.dominio.Reglas
import cl.apiavengers.lince.backend.seguridad.Password
import java.sql.Connection
import java.time.ZonedDateTime

object Esquema {
    val ddl: List<String> = listOf(
        """
        CREATE TABLE IF NOT EXISTS usuarios (
            id TEXT PRIMARY KEY,
            email TEXT NOT NULL UNIQUE,
            nombre TEXT NOT NULL,
            rol TEXT NOT NULL,
            password_hash TEXT NOT NULL,
            activo INTEGER NOT NULL DEFAULT 1
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS servicios_referencia (
            id TEXT PRIMARY KEY,
            tipo TEXT NOT NULL,
            titulo TEXT NOT NULL DEFAULT '',
            fecha_hora_pickup TEXT NOT NULL,
            estado TEXT NOT NULL,
            actualizado_en TEXT NOT NULL
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS asignaciones_referencia (
            servicio_id TEXT NOT NULL,
            usuario_id TEXT NOT NULL,
            fecha TEXT NOT NULL,
            PRIMARY KEY (servicio_id, usuario_id),
            FOREIGN KEY (servicio_id) REFERENCES servicios_referencia(id)
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS geocercas (
            id TEXT PRIMARY KEY,
            servicio_id TEXT,
            tipo TEXT NOT NULL,
            nombre TEXT NOT NULL,
            lat REAL NOT NULL,
            lng REAL NOT NULL,
            radio_m INTEGER NOT NULL DEFAULT 150,
            creado_por TEXT NOT NULL,
            creado_en TEXT NOT NULL,
            actualizado_en TEXT NOT NULL
        )
        """.trimIndent(),
        "CREATE UNIQUE INDEX IF NOT EXISTS ux_geocerca_servicio_tipo " +
            "ON geocercas(servicio_id, tipo) WHERE servicio_id IS NOT NULL",
        """
        CREATE TABLE IF NOT EXISTS hitos (
            id TEXT PRIMARY KEY,
            servicio_id TEXT NOT NULL,
            geocerca_id TEXT,
            usuario_id TEXT NOT NULL,
            tipo TEXT NOT NULL,
            lat REAL,
            lng REAL,
            precision_m REAL,
            distancia_m REAL,
            ocurrido_en TEXT NOT NULL,
            observaciones TEXT,
            motivo_rechazo TEXT,
            request_id TEXT,
            estado_sync TEXT NOT NULL DEFAULT 'SYNCED',
            creado_en TEXT NOT NULL
        )
        """.trimIndent(),
        "CREATE INDEX IF NOT EXISTS ix_hitos_servicio ON hitos(servicio_id, ocurrido_en DESC)",
        """
        CREATE TABLE IF NOT EXISTS disponibilidad (
            id TEXT PRIMARY KEY,
            usuario_id TEXT NOT NULL,
            fecha TEXT NOT NULL,
            motivo TEXT,
            creado_en TEXT NOT NULL,
            estado TEXT NOT NULL DEFAULT 'ACTIVO',
            UNIQUE (usuario_id, fecha)
        )
        """.trimIndent(),
        """
        CREATE TABLE IF NOT EXISTS incidencias (
            id TEXT PRIMARY KEY,
            servicio_id TEXT NOT NULL,
            usuario_id TEXT NOT NULL,
            categoria TEXT NOT NULL,
            descripcion TEXT NOT NULL,
            lat REAL,
            lng REAL,
            ocurrido_en TEXT NOT NULL,
            creado_en TEXT NOT NULL,
            estado TEXT NOT NULL DEFAULT 'ABIERTA'
        )
        """.trimIndent(),
        "CREATE INDEX IF NOT EXISTS ix_incidencias_servicio ON incidencias(servicio_id, ocurrido_en DESC)"
    )
}

object Semilla {
    fun aplicar(conexion: Connection) {
        val ahora = ZonedDateTime.now(Reglas.ZONA).withSecond(0).withNano(0)
        val hoy = ahora.toLocalDate()
        fun instante(dias: Long, hora: Int, minuto: Int): String =
            hoy.plusDays(dias).atTime(hora, minuto).atZone(Reglas.ZONA).toInstant().toString()

        conexion.actualizar(
            "INSERT OR IGNORE INTO usuarios (id, email, nombre, rol, password_hash) VALUES (?, ?, ?, ?, ?)",
            "USR-GUIA", "guia@lince.local", "Guía Demo", "GUIA", Password.hash(Config.passwordDemo)
        )
        conexion.actualizar(
            "INSERT OR IGNORE INTO usuarios (id, email, nombre, rol, password_hash) VALUES (?, ?, ?, ?, ?)",
            "USR-TRANSPORTISTA", "transportista@lince.local", "Transportista Demo",
            "TRANSPORTISTA", Password.hash(Config.passwordDemo)
        )
        conexion.actualizar(
            "INSERT OR IGNORE INTO usuarios (id, email, nombre, rol, password_hash) VALUES (?, ?, ?, ?, ?)",
            "USR-COORD", "coordinacion@lince.local", "Coordinación Demo",
            "COORDINACION", Password.hash(Config.passwordDemo)
        )

        val servicios = listOf(
            arrayOf<Any?>("LIN-001", "TRANSFER", "Traslado aeropuerto", instante(1, 8, 0), "CONFIRMADO"),
            arrayOf("LIN-002", "GUIDED_SERVICE", "City tour", ahora.plusHours(2).toInstant().toString(), "CONFIRMADO"),
            arrayOf("LIN-003", "EXCURSION", "Valle nevado", instante(3, 9, 0), "PENDIENTE")
        )
        servicios.forEach { fila ->
            conexion.actualizar(
                "INSERT OR IGNORE INTO servicios_referencia " +
                    "(id, tipo, titulo, fecha_hora_pickup, estado, actualizado_en) VALUES (?, ?, ?, ?, ?, ?)",
                *fila, ahora.toInstant().toString()
            )
        }

        val asignaciones = listOf(
            arrayOf<Any?>("LIN-001", "USR-GUIA", hoy.plusDays(1).toString()),
            arrayOf("LIN-002", "USR-TRANSPORTISTA", hoy.toString()),
            arrayOf("LIN-003", "USR-GUIA", hoy.plusDays(3).toString())
        )
        asignaciones.forEach { fila ->
            conexion.actualizar(
                "INSERT OR IGNORE INTO asignaciones_referencia (servicio_id, usuario_id, fecha) VALUES (?, ?, ?)",
                *fila
            )
        }

        val geocercas = listOf(
            arrayOf<Any?>("GEO-LIN-001-PICKUP", "LIN-001", "PICKUP", "Pickup hotel centro",
                -33.4372, -70.6506, 150),
            arrayOf<Any?>("GEO-LIN-001-HOTEL", "LIN-001", "HOTEL", "Hotel Plaza",
                -33.4350, -70.6480, 150),
            arrayOf<Any?>("GEO-LIN-002-PICKUP", "LIN-002", "PICKUP", "Pickup Plaza de Armas",
                -33.4372, -70.6506, 150)
        )
        geocercas.forEach { fila ->
            conexion.actualizar(
                "INSERT OR IGNORE INTO geocercas (id, servicio_id, tipo, nombre, lat, lng, radio_m, " +
                    "creado_por, creado_en, actualizado_en) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                *fila, "USR-COORD", ahora.toInstant().toString(), ahora.toInstant().toString()
            )
        }
    }
}
