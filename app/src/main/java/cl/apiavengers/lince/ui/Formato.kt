package cl.apiavengers.lince.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val LOCALE_CL = Locale("es", "CL")

fun fechaLargaHoy(): String = fechaLarga(LocalDate.now().toString())

fun fechaLarga(iso: String): String = try {
    val fecha = LocalDate.parse(iso)
    val texto = fecha.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", LOCALE_CL))
    texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase(LOCALE_CL) else it.toString() }
} catch (_: DateTimeParseException) {
    iso
}

fun fechaCorta(iso: String): String = try {
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("d MMM", LOCALE_CL))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(LOCALE_CL) else it.toString() }
        .uppercase(LOCALE_CL)
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
