package cl.apiavengers.lince.datos

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import java.io.File
import java.security.MessageDigest
import java.time.LocalDate

class ArchivoDisponibilidad(private val context: Context) : AlmacenDisponibilidad {
    private fun archivo(cuenta: String): AtomicFile {
        val clave = MessageDigest.getInstance("SHA-256")
            .digest(cuenta.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
        return AtomicFile(File(context.filesDir, "agenda_$clave.json"))
    }

    override fun leer(cuenta: String): Set<LocalDate> {
        val archivo = archivo(cuenta)
        if (!archivo.baseFile.exists() && !File(archivo.baseFile.path + ".bak").exists()) return emptySet()
        val texto = archivo.openRead().bufferedReader(Charsets.UTF_8).use { it.readText() }
        val fechas = JSONArray(texto)
        return (0 until fechas.length()).map { LocalDate.parse(fechas.getString(it)) }.toSet()
    }

    override fun guardar(cuenta: String, fechas: Set<LocalDate>) {
        val archivo = archivo(cuenta)
        val salida = archivo.startWrite()
        try {
            val texto = JSONArray(fechas.sorted().map { it.toString() }).toString()
            salida.write(texto.toByteArray(Charsets.UTF_8))
            archivo.finishWrite(salida)
        } catch (error: Exception) {
            archivo.failWrite(salida)
            throw error
        }
    }
}
