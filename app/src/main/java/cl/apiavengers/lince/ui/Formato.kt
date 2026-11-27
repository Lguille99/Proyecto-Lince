package cl.apiavengers.lince.ui

import cl.apiavengers.lince.ui.idioma.IdiomaApp
import cl.apiavengers.lince.ui.idioma.PreferenciaIdioma
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val LOCALE_CL = Locale("es", "CL")
private val LOCALE_EN = Locale("en", "US")
private val ZONA_SANTIAGO = ZoneId.of("America/Santiago")

private val FORMATO_LARGA_CL = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", LOCALE_CL)
private val FORMATO_LARGA_EN = DateTimeFormatter.ofPattern("EEEE, MMMM d", LOCALE_EN)
private val FORMATO_CORTA_CL = DateTimeFormatter.ofPattern("d MMM", LOCALE_CL)
private val FORMATO_CORTA_EN = DateTimeFormatter.ofPattern("MMM d", LOCALE_EN)
private val FORMATO_MES_CL = DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_CL)
private val FORMATO_MES_EN = DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE_EN)
private val FORMATO_NUMERICA = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val FORMATO_SELLO = DateTimeFormatter.ofPattern("dd/MM · HH:mm")
private val FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm")

private fun localeActual(): Locale =
    if (PreferenciaIdioma.idioma.value == IdiomaApp.INGLES) LOCALE_EN else LOCALE_CL

fun fechaLargaHoy(): String = fechaLarga(LocalDate.now(ZONA_SANTIAGO).toString())

fun fechaLarga(iso: String): String = try {
    val locale = localeActual()
    val formatter = if (locale == LOCALE_EN) FORMATO_LARGA_EN else FORMATO_LARGA_CL
    val texto = LocalDate.parse(iso).format(formatter)
    texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
} catch (_: DateTimeParseException) {
    iso
}

fun fechaCorta(iso: String): String = try {
    val locale = localeActual()
    val formatter = if (locale == LOCALE_EN) FORMATO_CORTA_EN else FORMATO_CORTA_CL
    LocalDate.parse(iso).format(formatter)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        .uppercase(locale)
} catch (_: DateTimeParseException) {
    iso
}

fun fechaNumerica(iso: String): String = try {
    LocalDate.parse(iso).format(FORMATO_NUMERICA)
} catch (_: DateTimeParseException) {
    iso
}

fun selloFechaHora(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        LocalDateTime.parse(iso).format(FORMATO_SELLO)
    } catch (_: DateTimeParseException) {
        iso
    }
}

fun horaMinuto(fecha: LocalDateTime): String = fecha.format(FORMATO_HORA)

fun mesLargo(mes: YearMonth): String {
    val locale = localeActual()
    val formatter = if (locale == LOCALE_EN) FORMATO_MES_EN else FORMATO_MES_CL
    val texto = mes.format(formatter)
    return texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

fun horaCorta(isoHora: String?): String = isoHora ?: "—"

fun montoClp(valor: Int): String = "$" + "%,d".format(LOCALE_CL, valor).replace(',', '.')

fun esFechaHoy(iso: String): Boolean = iso == LocalDate.now(ZONA_SANTIAGO).toString()

fun inicioPermitido(servicio: cl.apiavengers.lince.modelo.Servicio): LocalDateTime =
    servicio.inicioProgramado.minusMinutes(15)
