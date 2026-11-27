package cl.apiavengers.lince.datos.southbound

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Normaliza el parseo y formateo de fechas/horas de Southbound en la zona
 * horaria oficial de Chile continental (America/Santiago).
 */
object FechasSouthbound {
    val ZONA: ZoneId = ZoneId.of("America/Santiago")

    private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun fecha(valor: String): LocalDate = LocalDate.parse(valor, FORMATO_FECHA)

    fun hora(valor: String): LocalTime = LocalTime.parse(valor, FORMATO_HORA)

    fun formatearFecha(valor: LocalDate): String = valor.format(FORMATO_FECHA)

    fun formatearHora(valor: LocalTime): String = valor.format(FORMATO_HORA)

    /** Devuelve la hora en HH:mm; si el texto no calza, lo entrega sin cambios. */
    fun formatearHora(valor: String): String = try {
        formatearHora(hora(valor))
    } catch (_: DateTimeParseException) {
        valor
    }

    fun ahora(): ZonedDateTime = ZonedDateTime.now(ZONA)

    fun hoy(): LocalDate = LocalDate.now(ZONA)

    /**
     * Convierte un sello ISO (con o sin offset) a la zona de Chile.
     * Si no se puede interpretar, devuelve el texto original.
     */
    fun normalizarSello(valor: String): LocalDateTime = try {
        OffsetDateTime.parse(valor).atZoneSameInstant(ZONA).toLocalDateTime()
    } catch (_: DateTimeParseException) {
        try {
            ZonedDateTime.parse(valor).withZoneSameInstant(ZONA).toLocalDateTime()
        } catch (_: DateTimeParseException) {
            LocalDateTime.parse(valor)
        }
    }
}
