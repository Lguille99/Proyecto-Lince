package cl.apiavengers.lince.modelo

enum class EstadoServicio(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    COMPLETADA("Completada"),
    RECHAZADA("Rechazada")
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
    val historico: Boolean = false,
    val motivoRechazo: String? = null,
    val respondidoEn: String? = null
)

data class ResumenCoordinador(
    val serviciosActivos: Int,
    val pendientes: Int,
    val personasEquipo: Int,
    val personasDisponibles: Int,
    val historialTotal: Int
)
