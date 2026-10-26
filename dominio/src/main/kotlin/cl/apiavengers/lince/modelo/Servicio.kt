package cl.apiavengers.lince.modelo

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class EstadoServicio(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    COMPLETADA("Completada"),
    RECHAZADA("Rechazada")
}

enum class Idioma(val etiqueta: String) {
    ESPANOL("Español"),
    INGLES("Inglés")
}

enum class Modalidad(val etiqueta: String) {
    TRANSFER("Transfer"),
    MEDIO_DIA("Medio día"),
    DIA_COMPLETO("Día completo")
}

data class Servicio(
    val id: String,
    val titulo: String,
    val tipo: String,
    val fecha: String,
    val hora: String,
    val recogida: String,
    val destino: String,
    val asignadoA: String,
    val estado: EstadoServicio = EstadoServicio.PENDIENTE,
    val acompananteId: String? = null,
    val idiomaRequerido: Idioma = Idioma.ESPANOL,
    val modalidad: Modalidad = Modalidad.TRANSFER,
    val almuerzoIncluido: Boolean = false,
    val itinerario: List<String> = emptyList(),
    val resenaHistorica: String = "",
    val hotelRecogida: String = recogida,
    val tarifa: Int = 0,
    val pasajeros: Int = 1,
    val diasTour: Int = 1,
    val respondidoPor: String? = null,
    val respondidoEn: String? = null,
    val motivoRechazo: String? = null,
    val inicioEn: String? = null,
    val terminoEn: String? = null,
    val inicioPor: String? = null,
    val ubicacionInicio: String? = null,
    val observaciones: String = "",
    val puntaje: Int? = null,
    val calificadoPor: String? = null,
    val calificadoEn: String? = null,
    val reasignadoPor: String? = null,
    val reasignadoEn: String? = null
) {
    val inicioProgramado: LocalDateTime
        get() = LocalDateTime.of(LocalDate.parse(fecha), LocalTime.parse(hora))

    fun esDe(usuarioId: String): Boolean =
        asignadoA == usuarioId || acompananteId == usuarioId

    fun esTitular(usuarioId: String): Boolean = asignadoA == usuarioId
}

data class ResumenCoordinador(
    val serviciosActivos: Int,
    val pendientes: Int,
    val confirmadas: Int,
    val rechazadas: Int,
    val enEjecucion: Int,
    val personasEquipo: Int,
    val personasDisponibles: Int,
    val historialTotal: Int
)
