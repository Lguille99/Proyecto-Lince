package cl.apiavengers.lince.backend.db

import cl.apiavengers.lince.backend.Config
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object Db {
    init {
        Class.forName("org.sqlite.JDBC")
    }

    fun inicializar() {
        File(Config.rutaDb).absoluteFile.parentFile?.mkdirs()
        usar { conexion ->
            Esquema.ddl.forEach { sql ->
                conexion.createStatement().use { it.execute(sql) }
            }
            Semilla.aplicar(conexion)
        }
    }

    fun <T> usar(bloque: (Connection) -> T): T =
        DriverManager.getConnection("jdbc:sqlite:${Config.rutaDb}").use { conexion ->
            conexion.createStatement().use { it.execute("PRAGMA foreign_keys = ON") }
            conexion.createStatement().use { it.execute("PRAGMA journal_mode = WAL") }
            bloque(conexion)
        }
}

fun Connection.actualizar(sql: String, vararg parametros: Any?): Int =
    prepareStatement(sql).use { sentencia ->
        parametros.forEachIndexed { indice, valor ->
            sentencia.setObject(indice + 1, valor)
        }
        sentencia.executeUpdate()
    }

fun <T> Connection.consultar(
    sql: String,
    vararg parametros: Any?,
    mapa: (ResultSet) -> T
): List<T> = prepareStatement(sql).use { sentencia ->
    parametros.forEachIndexed { indice, valor ->
        sentencia.setObject(indice + 1, valor)
    }
    sentencia.executeQuery().use { filas ->
        buildList {
            while (filas.next()) add(mapa(filas))
        }
    }
}

fun <T> Connection.consultarUno(
    sql: String,
    vararg parametros: Any?,
    mapa: (ResultSet) -> T
): T? = consultar(sql, *parametros, mapa = mapa).firstOrNull()
