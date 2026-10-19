package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.*
import java.time.LocalDate

class RepositorioLince {
    private var usuarioActual: Usuario? = null
    private val servicios = DatosPrueba.servicios(LocalDate.now())

    fun iniciarSesion(cuenta: String, clave: String): Usuario {
        usuarioActual = null
        val usuario = DatosPrueba.usuarios.find { it.cuenta == cuenta.trim() }
        require(usuario != null && clave == DatosPrueba.CLAVE_DEMO) {
            "Usuario o contraseña incorrectos."
        }
        usuarioActual = usuario
        return usuario
    }

    fun consultarServicios(): List<Servicio> {
        val usuario = usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        return when (usuario.rol) {
            Rol.COORDINADOR -> servicios
            else -> servicios.filter { it.asignadoA == usuario.id }
        }
    }

    fun consultarEquipo(): List<Usuario> {
        usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        return DatosPrueba.usuarios
    }

    fun obtenerUsuarioPorId(id: String): Usuario {
        usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        return DatosPrueba.usuarios.find { it.id == id }
            ?: throw IllegalArgumentException("Persona no encontrada: $id")
    }

    fun obtenerServicioPorId(id: String): Servicio {
        usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        return servicios.find { it.id == id }
            ?: throw IllegalArgumentException("Servicio no encontrado: $id")
    }

    fun consultarServiciosDe(usuarioId: String): List<Servicio> {
        usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        return servicios.filter { it.asignadoA == usuarioId }
    }

    fun cerrarSesion() {
        usuarioActual = null
    }
}
