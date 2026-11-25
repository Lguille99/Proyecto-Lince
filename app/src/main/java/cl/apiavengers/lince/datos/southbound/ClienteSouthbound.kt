package cl.apiavengers.lince.datos.southbound

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class ErrorSouthbound(
    val codigo: String,
    val requestId: String? = null,
    val codigoServidor: String? = null,
    val mensajeServidor: String? = null,
    val detalles: List<String> = emptyList()
) : IOException(codigo)

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
            val contenido = if (codigo in 200..299) {
                conexion.inputStream.bufferedReader().use { it.readText() }
            } else {
                conexion.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
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
    suspend fun servicios(
        pagina: Int,
        busqueda: String = "",
        estadoServicio: String = "",
        estadoReserva: String = "",
        statusPL: String = "",
        tipo: String = "",
        oficina: String = "",
        clienteId: Int? = null,
        guiaId: Int? = null,
        transportistaId: Int? = null,
        fechaDesde: String = "",
        fechaHasta: String = "",
        orden: String = "",
        pageSize: Int = 20
    ): ConsultaResultado<PaginaSouthbound<ServicioSouthbound>> {
        exigir(pagina >= 1, "page debe ser mayor o igual a 1.")
        exigir(pageSize in 1..100, "pageSize debe ser un entero entre 1 y 100.")
        exigir(estadoServicio in ESTADOS, "estadoServicio inválido.")
        exigir(estadoReserva in ESTADOS, "estadoReserva inválido.")
        exigir(statusPL in STATUS_PL, "statusPL inválido.")
        exigir(tipo in TIPOS_SERVICIO, "tipo inválido.")
        exigir(orden in ORDENES, "orden inválido.")
        validarFechas(fechaDesde, fechaHasta)
        val ruta = "/api/v1/servicios" + consulta(
            "page" to pagina.toString(),
            "pageSize" to pageSize.toString(),
            "q" to busqueda.trim().takeIf { it.isNotEmpty() },
            "estadoServicio" to estadoServicio.takeIf { it.isNotEmpty() },
            "estadoReserva" to estadoReserva.takeIf { it.isNotEmpty() },
            "statusPL" to statusPL.takeIf { it.isNotEmpty() },
            "tipo" to tipo.takeIf { it.isNotEmpty() },
            "oficina" to oficina.takeIf { it.isNotEmpty() },
            "clienteId" to clienteId?.toString(),
            "guiaId" to guiaId?.toString(),
            "transportistaId" to transportistaId?.toString(),
            "fechaDesde" to fechaDesde.takeIf { it.isNotEmpty() },
            "fechaHasta" to fechaHasta.takeIf { it.isNotEmpty() },
            "orden" to orden.takeIf { it.isNotEmpty() }
        )
        return obtener(ruta, ServicioSouthbound::paginaDesdeJson)
    }

    suspend fun detalle(id: Int): ConsultaResultado<ServicioSouthbound> {
        exigir(id > 0, "El identificador del servicio debe ser mayor que 0.")
        return obtener("/api/v1/servicios/$id") { ServicioSouthbound.desdeJson(objetoData(it)) }
    }

    suspend fun guias(
        pagina: Int,
        busqueda: String = "",
        oficina: String = "",
        idioma: String = "",
        activo: Boolean? = null,
        pageSize: Int = 20
    ): ConsultaResultado<PaginaSouthbound<GuiaSouthbound>> {
        exigir(pagina >= 1, "page debe ser mayor o igual a 1.")
        exigir(pageSize in 1..100, "pageSize debe ser un entero entre 1 y 100.")
        exigir(oficina in OFICINAS, "oficina inválida.")
        exigir(idioma in IDIOMAS, "idioma inválido.")
        val ruta = "/api/v1/guias" + consulta(
            "page" to pagina.toString(),
            "pageSize" to pageSize.toString(),
            "q" to busqueda.trim().takeIf { it.isNotEmpty() },
            "oficina" to oficina.takeIf { it.isNotEmpty() },
            "idioma" to idioma.takeIf { it.isNotEmpty() },
            "activo" to activo?.toString()
        )
        return obtener(ruta, GuiaSouthbound::paginaDesdeJson)
    }

    suspend fun guia(id: Int): ConsultaResultado<GuiaSouthbound> {
        exigir(id > 0, "El identificador del guía debe ser mayor que 0.")
        return obtener("/api/v1/guias/$id") { GuiaSouthbound.desdeJson(objetoData(it)) }
    }

    suspend fun transportistas(
        pagina: Int,
        busqueda: String = "",
        oficina: String = "",
        activo: Boolean? = null,
        capacidadMin: Int? = null,
        pageSize: Int = 20
    ): ConsultaResultado<PaginaSouthbound<TransportistaSouthbound>> {
        exigir(pagina >= 1, "page debe ser mayor o igual a 1.")
        exigir(pageSize in 1..100, "pageSize debe ser un entero entre 1 y 100.")
        exigir(oficina in OFICINAS, "oficina inválida.")
        exigir(capacidadMin == null || capacidadMin in 1..100, "capacidadMin debe estar entre 1 y 100.")
        val ruta = "/api/v1/transportistas" + consulta(
            "page" to pagina.toString(),
            "pageSize" to pageSize.toString(),
            "q" to busqueda.trim().takeIf { it.isNotEmpty() },
            "oficina" to oficina.takeIf { it.isNotEmpty() },
            "activo" to activo?.toString(),
            "capacidadMin" to capacidadMin?.toString()
        )
        return obtener(ruta, TransportistaSouthbound::paginaDesdeJson)
    }

    suspend fun transportista(id: Int): ConsultaResultado<TransportistaSouthbound> {
        exigir(id > 0, "El identificador del transportista debe ser mayor que 0.")
        return obtener("/api/v1/transportistas/$id") {
            TransportistaSouthbound.desdeJson(objetoData(it))
        }
    }

    suspend fun clientes(
        pagina: Int,
        busqueda: String = "",
        pais: String = "",
        pageSize: Int = 20
    ): ConsultaResultado<PaginaSouthbound<ClienteApiSouthbound>> {
        exigir(pagina >= 1, "page debe ser mayor o igual a 1.")
        exigir(pageSize in 1..100, "pageSize debe ser un entero entre 1 y 100.")
        val ruta = "/api/v1/clientes" + consulta(
            "page" to pagina.toString(),
            "pageSize" to pageSize.toString(),
            "q" to busqueda.trim().takeIf { it.isNotEmpty() },
            "pais" to pais.trim().takeIf { it.isNotEmpty() }
        )
        return obtener(ruta, ClienteApiSouthbound::paginaDesdeJson)
    }

    suspend fun cliente(id: Int): ConsultaResultado<ClienteApiSouthbound> {
        exigir(id > 0, "El identificador del cliente debe ser mayor que 0.")
        return obtener("/api/v1/clientes/$id") { ClienteApiSouthbound.desdeJson(objetoData(it)) }
    }

    suspend fun catalogos(): ConsultaResultado<CatalogoSouthbound> =
        obtener("/api/v1/catalogos") { CatalogoSouthbound.desdeJson(objetoData(it)) }

    suspend fun resumen(
        fechaDesde: String = "",
        fechaHasta: String = "",
        oficina: String = ""
    ): ConsultaResultado<ResumenSouthbound> {
        exigir(oficina in OFICINAS, "oficina inválida.")
        validarFechas(fechaDesde, fechaHasta)
        val ruta = "/api/v1/resumen" + consulta(
            "fechaDesde" to fechaDesde.takeIf { it.isNotEmpty() },
            "fechaHasta" to fechaHasta.takeIf { it.isNotEmpty() },
            "oficina" to oficina.takeIf { it.isNotEmpty() }
        )
        return obtener(ruta, { ResumenSouthbound.desdeJson(objetoData(it)) })
    }

    suspend fun salud(): ConsultaResultado<SaludSouthbound> =
        obtener("/health") { SaludSouthbound.desdeJson(JSONObject(it)) }

    private suspend fun <T> obtener(ruta: String, convertir: (String) -> T): ConsultaResultado<T> =
        withContext(Dispatchers.IO) {
            if (clave.isBlank()) throw ErrorSouthbound("configuracion")
            val respuesta = try {
                transporte.consultar(ruta, clave)
            } catch (error: IOException) {
                return@withContext copia(ruta, convertir, ErrorSouthbound("conexion"))
            }
            when (respuesta.codigo) {
                200 -> {
                    val datos = try {
                        convertir(respuesta.contenido)
                    } catch (error: JSONException) {
                        throw ErrorSouthbound("formato", mensajeServidor = error.message)
                    } catch (error: Exception) {
                        throw ErrorSouthbound("formato", mensajeServidor = error.message)
                    }
                    val fecha = System.currentTimeMillis()
                    cache.guardar(ConsultaSouthbound(ruta, respuesta.contenido, fecha))
                    ConsultaResultado(datos, false, fecha)
                }
                400 -> throw errorDeRespuesta(respuesta, "validacion")
                401, 403 -> throw errorDeRespuesta(respuesta, "clave")
                404 -> throw errorDeRespuesta(respuesta, "no_encontrado")
                429 -> copia(ruta, convertir, errorDeRespuesta(respuesta, "limite"))
                in 500..599 -> copia(ruta, convertir, errorDeRespuesta(respuesta, "servidor"))
                else -> throw errorDeRespuesta(respuesta, "solicitud")
            }
        }

    private fun <T> copia(
        ruta: String,
        convertir: (String) -> T,
        error: ErrorSouthbound
    ): ConsultaResultado<T> {
        val consulta = cache.leer(ruta) ?: throw error
        return try {
            ConsultaResultado(convertir(consulta.contenido), true, consulta.guardadoEn)
        } catch (problema: JSONException) {
            throw ErrorSouthbound("formato", mensajeServidor = problema.message)
        }
    }

    private fun errorDeRespuesta(respuesta: RespuestaHttp, categoria: String): ErrorSouthbound {
        val cuerpo = try {
            JSONObject(respuesta.contenido).getJSONObject("error")
        } catch (_: Exception) {
            null
        }
        val detalles = cuerpo?.optJSONArray("details")?.let { arreglo ->
            List(arreglo.length()) { indice ->
                val detalle = arreglo.getJSONObject(indice)
                listOfNotNull(
                    detalle.textoOpcional("field"),
                    detalle.textoOpcional("message")
                ).joinToString(": ")
            }
        }.orEmpty()
        return ErrorSouthbound(
            codigo = categoria,
            requestId = cuerpo?.textoOpcional("requestId"),
            codigoServidor = cuerpo?.textoOpcional("code"),
            mensajeServidor = cuerpo?.textoOpcional("message"),
            detalles = detalles
        )
    }

    private fun consulta(vararg pares: Pair<String, Any?>): String {
        val partes = pares.mapNotNull { (nombre, valor) ->
            val texto = valor?.toString()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            "$nombre=${URLEncoder.encode(texto, "UTF-8")}"
        }
        return if (partes.isEmpty()) "" else "?" + partes.joinToString("&")
    }

    private fun validarFechas(fechaDesde: String, fechaHasta: String) {
        if (fechaDesde.isNotEmpty()) exigir(FECHA.matches(fechaDesde), "fechaDesde debe usar el formato YYYY-MM-DD.")
        if (fechaHasta.isNotEmpty()) exigir(FECHA.matches(fechaHasta), "fechaHasta debe usar el formato YYYY-MM-DD.")
        if (fechaDesde.isNotEmpty() && fechaHasta.isNotEmpty() && fechaDesde > fechaHasta) {
            exigir(false, "fechaDesde no puede ser posterior a fechaHasta.")
        }
    }

    private fun exigir(condicion: Boolean, mensaje: String) {
        if (!condicion) throw ErrorSouthbound("validacion", mensajeServidor = mensaje)
    }

    private companion object {
        val FECHA = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        val ESTADOS = setOf("", "CF", "RQ", "WL", "XX")
        val STATUS_PL = setOf("", "CF", "RQ", "DL", "CX", "SIN_ASIGNAR")
        val TIPOS_SERVICIO = setOf("", "TRANSFER", "GUIDED_SERVICE", "EXCURSION")
        val OFICINAS = setOf("", "SCL", "PUX", "PUQ")
        val IDIOMAS = setOf("", "ES", "EN", "PT", "DE", "FR")
        val ORDENES = setOf("", "fecha", "-fecha", "pax", "-pax")
    }
}
