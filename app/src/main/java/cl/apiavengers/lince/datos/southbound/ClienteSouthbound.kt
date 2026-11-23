package cl.apiavengers.lince.datos.southbound

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class ErrorSouthbound(val codigo: String) : IOException(codigo)
data class RespuestaHttp(val codigo: Int, val contenido: String)

fun interface TransporteSouthbound {
    fun consultar(ruta: String, clave: String): RespuestaHttp
}

class TransporteHttpsSouthbound : TransporteSouthbound {
    override fun consultar(ruta: String, clave: String): RespuestaHttp {
        val conexion = URI("https://ws.southbound.travel/lince-api$ruta").toURL()
            .openConnection() as HttpURLConnection
        try {
            conexion.requestMethod = "GET"
            conexion.instanceFollowRedirects = false
            conexion.connectTimeout = 15000
            conexion.readTimeout = 15000
            conexion.setRequestProperty("Accept", "application/json")
            conexion.setRequestProperty("X-API-Key", clave)
            val codigo = conexion.responseCode
            val contenido = if (codigo == 200) {
                conexion.inputStream.bufferedReader().use { it.readText() }
            } else ""
            return RespuestaHttp(codigo, contenido)
        } finally {
            conexion.disconnect()
        }
    }
}

data class ConsultaResultado<T>(val datos: T, val esCopia: Boolean, val guardadoEn: Long)

class ClienteSouthbound(
    private val clave: String,
    private val cache: CacheSouthboundDao,
    private val transporte: TransporteSouthbound = TransporteHttpsSouthbound()
) {
    suspend fun servicios(pagina: Int, busqueda: String, estado: String): ConsultaResultado<PaginaSouthbound> {
        require(pagina >= 1)
        require(estado in listOf("", "CF", "RQ", "WL", "XX"))
        val consulta = URLEncoder.encode(busqueda.trim(), "UTF-8")
        val ruta = "/api/v1/servicios?page=$pagina&pageSize=20" +
            (if (consulta.isNotEmpty()) "&q=$consulta" else "") +
            (if (estado.isNotEmpty()) "&estadoServicio=$estado" else "")
        return obtener(ruta, PaginaSouthbound::desdeJson)
    }

    suspend fun detalle(id: Int): ConsultaResultado<ServicioSouthbound> {
        require(id > 0)
        return obtener("/api/v1/servicios/$id") {
            ServicioSouthbound.desdeJson(JSONObject(it).getJSONObject("data"))
        }
    }

    private suspend fun <T> obtener(ruta: String, convertir: (String) -> T): ConsultaResultado<T> =
        withContext(Dispatchers.IO) {
            if (clave.isBlank()) throw ErrorSouthbound("configuracion")
            val respuesta = try {
                transporte.consultar(ruta, clave)
            } catch (error: IOException) {
                return@withContext copia(ruta, convertir, "conexion")
            }
            when (respuesta.codigo) {
                200 -> {
                    val datos = try {
                        convertir(respuesta.contenido)
                    } catch (error: JSONException) {
                        throw ErrorSouthbound("formato")
                    }
                    val fecha = System.currentTimeMillis()
                    cache.guardar(ConsultaSouthbound(ruta, respuesta.contenido, fecha))
                    ConsultaResultado(datos, false, fecha)
                }
                401, 403 -> throw ErrorSouthbound("clave")
                404 -> throw ErrorSouthbound("no_encontrado")
                429 -> copia(ruta, convertir, "limite")
                in 500..599 -> copia(ruta, convertir, "servidor")
                else -> throw ErrorSouthbound("solicitud")
            }
        }

    private fun <T> copia(ruta: String, convertir: (String) -> T, error: String): ConsultaResultado<T> {
        val consulta = cache.leer(ruta) ?: throw ErrorSouthbound(error)
        return try {
            ConsultaResultado(convertir(consulta.contenido), true, consulta.guardadoEn)
        } catch (problema: JSONException) {
            throw ErrorSouthbound("formato")
        }
    }
}
