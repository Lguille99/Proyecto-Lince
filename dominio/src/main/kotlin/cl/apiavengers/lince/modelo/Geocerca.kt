package cl.apiavengers.lince.modelo

import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Double? = null,
    val obtenidaEn: String? = null
) {
    val valida: Boolean
        get() = latitud in -90.0..90.0 && longitud in -180.0..180.0
}

data class EvaluacionGeocerca(
    val distanciaMetros: Double,
    val radioMetros: Int,
    val dentro: Boolean,
    val precisionMetros: Double?,
    val minutosAntes: Long,
    val tardanza: Boolean
) {
    val fueraDeZona: Boolean get() = !dentro
    val precisionBaja: Boolean get() = precisionMetros != null && precisionMetros > 100.0
}

const val RADIO_GEOCERCA_PREDETERMINADO_METROS = 150

fun distanciaMetros(latitud1: Double, longitud1: Double, latitud2: Double, longitud2: Double): Double {
    val radioTierra = 6_371_000.0
    val deltaLatitud = Math.toRadians(latitud2 - latitud1)
    val deltaLongitud = Math.toRadians(longitud2 - longitud1)
    val a = sin(deltaLatitud / 2).pow(2) +
        cos(Math.toRadians(latitud1)) * cos(Math.toRadians(latitud2)) *
        sin(deltaLongitud / 2).pow(2)
    return 2 * radioTierra * asin(sqrt(a.coerceIn(0.0, 1.0)))
}

fun evaluarLlegada(
    servicio: Servicio,
    ubicacion: Ubicacion,
    ahora: LocalDateTime = LocalDateTime.now()
): EvaluacionGeocerca? {
    val latitud = servicio.latitudRecogida ?: return null
    val longitud = servicio.longitudRecogida ?: return null
    val distancia = distanciaMetros(ubicacion.latitud, ubicacion.longitud, latitud, longitud)
    val radio = servicio.radioGeocercaMetros
    val minutosAntes = Duration.between(ahora, servicio.inicioProgramado).toMinutes()
    return EvaluacionGeocerca(
        distanciaMetros = distancia,
        radioMetros = radio,
        dentro = distancia <= radio,
        precisionMetros = ubicacion.precisionMetros,
        minutosAntes = minutosAntes,
        tardanza = ahora.isAfter(servicio.inicioProgramado)
    )
}
