package cl.apiavengers.lince.backend.dominio

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

object Reglas {
    const val RADIO_POR_DEFECTO_M = 150
    const val RADIO_MINIMO_M = 10
    const val RADIO_MAXIMO_M = 5_000
    const val MINUTOS_ANTICIPACION_BLOQUEO = 7L
    const val MINUTOS_PUNTUALIDAD = 15L
    const val RADIO_TIERRA_M = 6_371_000.0
    const val PRECISION_GPS_MAXIMA_M = 50.0

    val ZONA: ZoneId = ZoneId.of("America/Santiago")

    fun distanciaMetros(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
        return 2 * RADIO_TIERRA_M * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }

    fun dentroDeGeocerca(
        lat: Double,
        lng: Double,
        centroLat: Double,
        centroLng: Double,
        radioM: Int
    ): Boolean = distanciaMetros(lat, lng, centroLat, centroLng) <= radioM

    fun limitePuntual(pickup: Instant): Instant =
        pickup.minus(Duration.ofMinutes(MINUTOS_PUNTUALIDAD))

    fun esPuntual(pickup: Instant, ahora: Instant = Instant.now()): Boolean =
        !ahora.isAfter(limitePuntual(pickup))

    fun minutosDeTardanza(pickup: Instant, ahora: Instant = Instant.now()): Long =
        maxOf(0, Duration.between(limitePuntual(pickup), ahora).toMinutes())

    fun anticipacionSuficiente(fecha: LocalDate, hoy: LocalDate = LocalDate.now(ZONA)): Boolean =
        !fecha.isBefore(hoy.plusDays(MINUTOS_ANTICIPACION_BLOQUEO))

    fun parsearInstante(valor: String): Instant = try {
        Instant.parse(valor)
    } catch (_: Exception) {
        OffsetDateTime.parse(valor).toInstant()
    }

    fun fechaLocal(valor: String): LocalDate = LocalDate.parse(valor.take(10))

    fun precisionGpsSuficiente(precisionM: Double?): Boolean =
        precisionM == null || precisionM <= PRECISION_GPS_MAXIMA_M
}
