package cl.apiavengers.lince.datos

import android.content.Context
import android.util.AtomicFile
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ArchivoEstado(private val context: Context) : AlmacenEstado {

    private fun archivo(): AtomicFile = AtomicFile(File(context.filesDir, "estado_lince.json"))

    override fun leer(): EstadoGuardado {
        val base = archivo()
        val respaldo = File(base.baseFile.path + ".bak")
        if (!base.baseFile.exists() && !respaldo.exists()) return EstadoGuardado()
        return try {
            val texto = base.openRead().bufferedReader(Charsets.UTF_8).use { it.readText() }
            parsear(JSONObject(texto))
        } catch (_: Exception) {
            EstadoGuardado()
        }
    }

    override fun guardar(estado: EstadoGuardado) {
        val archivo = archivo()
        val salida = archivo.startWrite()
        try {
            val texto = serializar(estado).toString()
            salida.write(texto.toByteArray(Charsets.UTF_8))
            archivo.finishWrite(salida)
        } catch (error: Exception) {
            archivo.failWrite(salida)
            throw error
        }
    }

    private fun serializar(estado: EstadoGuardado): JSONObject {
        val servicios = JSONObject()
        estado.servicios.forEach { (id, cambio) ->
            servicios.put(
                id,
                JSONObject().apply {
                    put("estado", cambio.estado.name)
                    put("asignadoA", cambio.asignadoA)
                    put("acompananteId", cambio.acompananteId ?: JSONObject.NULL)
                    put("respondidoPor", cambio.respondidoPor ?: JSONObject.NULL)
                    put("respondidoEn", cambio.respondidoEn ?: JSONObject.NULL)
                    put("motivoRechazo", cambio.motivoRechazo ?: JSONObject.NULL)
                    put("inicioEn", cambio.inicioEn ?: JSONObject.NULL)
                    put("terminoEn", cambio.terminoEn ?: JSONObject.NULL)
                    put("inicioPor", cambio.inicioPor ?: JSONObject.NULL)
                    put("ubicacionInicio", cambio.ubicacionInicio ?: JSONObject.NULL)
                    put("observaciones", cambio.observaciones)
                    put("puntaje", cambio.puntaje ?: JSONObject.NULL)
                    put("calificadoPor", cambio.calificadoPor ?: JSONObject.NULL)
                    put("calificadoEn", cambio.calificadoEn ?: JSONObject.NULL)
                    put("reasignadoPor", cambio.reasignadoPor ?: JSONObject.NULL)
                    put("reasignadoEn", cambio.reasignadoEn ?: JSONObject.NULL)
                }
            )
        }
        val perfiles = JSONObject()
        estado.perfiles.forEach { (id, cambio) ->
            perfiles.put(
                id,
                JSONObject().apply {
                    put("nombre", cambio.nombre)
                    put("correo", cambio.correo)
                    put("telefono", cambio.telefono)
                    put("zona", cambio.zona)
                    put("idiomas", JSONArray(cambio.idiomas.map { it.name }))
                }
            )
        }
        return JSONObject().apply {
            put("servicios", servicios)
            put("perfiles", perfiles)
        }
    }

    private fun parsear(raiz: JSONObject): EstadoGuardado {
        val servicios = mutableMapOf<String, CambioServicio>()
        raiz.optJSONObject("servicios")?.let { objeto ->
            objeto.keys().forEach { id ->
                val item = objeto.optJSONObject(id) ?: return@forEach
                servicios[id] = CambioServicio(
                    estado = enumSeguro(item.optString("estado"), EstadoServicio.PENDIENTE),
                    asignadoA = item.optString("asignadoA"),
                    acompananteId = textoNulo(item, "acompananteId"),
                    respondidoPor = textoNulo(item, "respondidoPor"),
                    respondidoEn = textoNulo(item, "respondidoEn"),
                    motivoRechazo = textoNulo(item, "motivoRechazo"),
                    inicioEn = textoNulo(item, "inicioEn"),
                    terminoEn = textoNulo(item, "terminoEn"),
                    inicioPor = textoNulo(item, "inicioPor"),
                    ubicacionInicio = textoNulo(item, "ubicacionInicio"),
                    observaciones = item.optString("observaciones"),
                    puntaje = if (item.isNull("puntaje")) null else item.optInt("puntaje"),
                    calificadoPor = textoNulo(item, "calificadoPor"),
                    calificadoEn = textoNulo(item, "calificadoEn"),
                    reasignadoPor = textoNulo(item, "reasignadoPor"),
                    reasignadoEn = textoNulo(item, "reasignadoEn")
                )
            }
        }
        val perfiles = mutableMapOf<String, CambioPerfil>()
        raiz.optJSONObject("perfiles")?.let { objeto ->
            objeto.keys().forEach { id ->
                val item = objeto.optJSONObject(id) ?: return@forEach
                val idiomas = mutableListOf<Idioma>()
                item.optJSONArray("idiomas")?.let { arreglo ->
                    for (indice in 0 until arreglo.length()) {
                        idiomas += enumSeguro(arreglo.optString(indice), Idioma.ESPANOL)
                    }
                }
                perfiles[id] = CambioPerfil(
                    nombre = item.optString("nombre"),
                    correo = item.optString("correo"),
                    telefono = item.optString("telefono"),
                    zona = item.optString("zona"),
                    idiomas = idiomas.ifEmpty { listOf(Idioma.ESPANOL) }
                )
            }
        }
        return EstadoGuardado(servicios, perfiles)
    }

    private fun textoNulo(objeto: JSONObject, clave: String): String? =
        if (!objeto.has(clave) || objeto.isNull(clave)) null else objeto.optString(clave)

    private inline fun <reified T : Enum<T>> enumSeguro(valor: String?, porDefecto: T): T =
        runCatching { enumValueOf<T>(valor.orEmpty()) }.getOrDefault(porDefecto)
}
