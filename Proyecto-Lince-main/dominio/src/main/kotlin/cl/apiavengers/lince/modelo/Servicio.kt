package cl.apiavengers.lince.modelo

// Información básica de una asignación de prueba.
data class Servicio(
    val id: String,
    val titulo: String,
    val tipo: String,
    val fecha: String,
    val hora: String,
    val recogida: String,
    val destino: String,
    val asignadoA: String
)
