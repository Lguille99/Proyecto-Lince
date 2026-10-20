package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.*
import java.time.LocalDate

class RepositorioLince {
    private var usuarioActual: Usuario? = null
    private val hoy = LocalDate.now()
    private val activos = DatosPrueba.servicios(hoy)
    private val historial = DatosPrueba.historial(hoy)
    private val todos = activos + historial

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
            Rol.COORDINADOR -> activos
            else -> activos.filter { it.asignadoA == usuario.id }
        }
    }

    fun consultarHistorial(): List<Servicio> {
        val usuario = usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        return when (usuario.rol) {
            Rol.COORDINADOR -> historial
            else -> historial.filter { it.asignadoA == usuario.id }
        }
    }

    fun consultarHistorialDe(usuarioId: String): List<Servicio> {
        usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        return historial.filter { it.asignadoA == usuarioId }
    }

    fun resumenCoordinador(): ResumenCoordinador {
        val usuario = usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        require(usuario.rol == Rol.COORDINADOR) { "Resumen disponible solo para coordinadores." }
        return ResumenCoordinador(
            serviciosActivos = activos.size,
            pendientes = activos.count { it.estado == EstadoServicio.PENDIENTE },
            personasEquipo = DatosPrueba.usuarios.size,
            personasDisponibles = DatosPrueba.usuarios.count { it.disponibilidad == Disponibilidad.DISPONIBLE },
            historialTotal = historial.size
        )
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
        return todos.find { it.id == id }
            ?: throw IllegalArgumentException("Servicio no encontrado: $id")
    }

    fun consultarServiciosDe(usuarioId: String): List<Servicio> {
        usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        return activos.filter { it.asignadoA == usuarioId }
    }

    fun cerrarSesion() {
        usuarioActual = null
    }
}
