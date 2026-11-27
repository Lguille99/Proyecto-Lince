package cl.apiavengers.lince.datos.backend

import cl.apiavengers.lince.datos.local.OperacionPendienteEntity
import cl.apiavengers.lince.datos.local.Sincronizador
import org.json.JSONObject

class SincronizadorBackend(
    private val api: BackendApi = BackendRed.api()
) : Sincronizador {
    override suspend fun enviar(operacion: OperacionPendienteEntity): Boolean {
        val token = TokenAlmacen.bearer() ?: return false
        return try {
            when (operacion.tipo) {
                "CONFIRMAR" -> {
                    val body = HitoRequestBackend(
                        servicioId = operacion.servicioId.orEmpty(),
                        tipo = "CONFIRMACION"
                    )
                    api.crearHito(token, body).isSuccessful
                }
                "RECHAZAR" -> {
                    val motivo = runCatching { JSONObject(operacion.detalle).optString("motivo") }.getOrNull()
                    val body = HitoRequestBackend(
                        servicioId = operacion.servicioId.orEmpty(),
                        tipo = "RECHAZO",
                        motivoRechazo = motivo
                    )
                    api.crearHito(token, body).isSuccessful
                }
                "CHECK_IN" -> {
                    val json = runCatching { JSONObject(operacion.detalle) }.getOrNull()
                    val body = CheckInRequestBackend(
                        servicioId = operacion.servicioId.orEmpty(),
                        lat = json?.optDouble("lat") ?: 0.0,
                        lng = json?.optDouble("lng") ?: 0.0,
                        precisionM = json?.optDouble("precision")?.takeIf { !it.isNaN() },
                        observaciones = json?.optString("observaciones")
                    )
                    api.checkIn(token, body).isSuccessful
                }
                "CHECK_OUT" -> {
                    val json = runCatching { JSONObject(operacion.detalle) }.getOrNull()
                    val body = CheckOutRequestBackend(
                        servicioId = operacion.servicioId.orEmpty(),
                        lat = json?.optDouble("lat") ?: 0.0,
                        lng = json?.optDouble("lng") ?: 0.0,
                        precisionM = json?.optDouble("precision")?.takeIf { !it.isNaN() },
                        observaciones = json?.optString("observaciones")
                    )
                    api.checkOut(token, body).isSuccessful
                }
                "DISPONIBILIDAD" -> {
                    val json = runCatching { JSONObject(operacion.detalle) }.getOrNull()
                    val body = DisponibilidadRequestBackend(
                        fecha = json?.optString("fecha").orEmpty(),
                        motivo = json?.optString("motivo")
                    )
                    api.bloquearDisponibilidad(token, body).isSuccessful
                }
                "REPORTAR_UBICACION" -> {
                    val json = runCatching { JSONObject(operacion.detalle) }.getOrNull()
                    val body = HitoRequestBackend(
                        servicioId = operacion.servicioId.orEmpty(),
                        tipo = "REPORTE_POSICION",
                        lat = json?.optDouble("lat"),
                        lng = json?.optDouble("lng"),
                        precisionM = json?.optDouble("precision")?.takeIf { !it.isNaN() }
                    )
                    api.crearHito(token, body).isSuccessful
                }
                else -> true
            }
        } catch (_: Exception) {
            false
        }
    }
}
