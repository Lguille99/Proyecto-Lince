package cl.apiavengers.lince.ui

import cl.apiavengers.lince.ui.idioma.IdiomaApp
import cl.apiavengers.lince.ui.idioma.PreferenciaIdioma
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val LOCALE_CL = Locale("es", "CL")
private val LOCALE_EN = Locale("en", "US")

private fun localeActual(): Locale =
    if (PreferenciaIdioma.idioma.value == IdiomaApp.INGLES) LOCALE_EN else LOCALE_CL

fun fechaLargaHoy(): String = fechaLarga(LocalDate.now().toString())

fun fechaLarga(iso: String): String = try {
    val fecha = LocalDate.parse(iso)
    val locale = localeActual()
    val patron = if (locale == LOCALE_EN) "EEEE, MMMM d" else "EEEE d 'de' MMMM"
    val texto = fecha.format(DateTimeFormatter.ofPattern(patron, locale))
    texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
} catch (_: DateTimeParseException) {
    iso
}

fun fechaCorta(iso: String): String = try {
    val locale = localeActual()
    val patron = if (locale == LOCALE_EN) "MMM d" else "d MMM"
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern(patron, locale))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        .uppercase(locale)
} catch (_: DateTimeParseException) {
    iso
}

fun fechaNumerica(iso: String): String = try {
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
} catch (_: DateTimeParseException) {
    iso
}

fun selloFechaHora(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        LocalDateTime.parse(iso).format(DateTimeFormatter.ofPattern("dd/MM · HH:mm"))
    } catch (_: DateTimeParseException) {
        iso
    }
}

fun horaCorta(isoHora: String?): String = isoHora ?: "—"

fun montoClp(valor: Int): String = "$" + "%,d".format(LOCALE_CL, valor).replace(',', '.')

fun esFechaHoy(iso: String): Boolean = iso == LocalDate.now().toString()

fun inicioPermitido(servicio: cl.apiavengers.lince.modelo.Servicio): LocalDateTime =
    servicio.inicioProgramado.minusMinutes(15)
