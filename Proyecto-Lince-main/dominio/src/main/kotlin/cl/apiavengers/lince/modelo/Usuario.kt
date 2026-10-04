package cl.apiavengers.lince.modelo

enum class Rol(val etiqueta: String) {
    CONDUCTOR("Conductor"), GUIA("Guía"), COORDINADOR("Coordinador")
}

data class Usuario(val id: String, val nombre: String, val cuenta: String, val rol: Rol)
