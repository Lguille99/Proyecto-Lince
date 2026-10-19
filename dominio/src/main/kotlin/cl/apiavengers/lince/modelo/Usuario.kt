package cl.apiavengers.lince.modelo

enum class Rol(val etiqueta: String) {
    CONDUCTOR("Conductor"), GUIA("Guía"), COORDINADOR("Coordinador")
}

enum class Disponibilidad(val etiqueta: String) {
    DISPONIBLE("Disponible"), EN_SERVICIO("En servicio"), NO_DISPONIBLE("No disponible")
}

data class Usuario(
    val id: String,
    val nombre: String,
    val cuenta: String,
    val correo: String,
    val rol: Rol,
    val telefono: String = "+56 9 0000 0000",
    val disponibilidad: Disponibilidad = Disponibilidad.DISPONIBLE,
    val zona: String = "Zona central"
)
