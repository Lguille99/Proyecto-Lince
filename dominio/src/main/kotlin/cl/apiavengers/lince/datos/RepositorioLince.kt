package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.*
import java.time.LocalDate

class RepositorioLince(private val almacen: AlmacenServicios = AlmacenServiciosMemoria()) {
    private var usuarioActual: Usuario? = null
    private val activos get() = almacen.consultar().filter { !it.historico }
    private val historial get() = almacen.consultar().filter { it.historico || it.respondidoEn != null }
    private val todos get() = almacen.consultar()

    private fun preparar() {
        val hoy = LocalDate.now()
        almacen.preparar(DatosPrueba.servicios(hoy) + DatosPrueba.historial(hoy).map { it.copy(historico = true) })
    }

    fun iniciarSesion(cuenta: String, clave: String): Usuario {
        usuarioActual = null
        val usuario = DatosPrueba.usuarios.find { it.cuenta == cuenta.trim() }
        require(usuario != null && clave == DatosPrueba.CLAVE_DEMO) {
            "Usuario o contraseña incorrectos."
        }
        preparar()
        usuarioActual = usuario
        return usuario
    }

    fun establecerUsuario(usuario: Usuario) {
        preparar()
        usuarioActual = usuario
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

    fun responderServicio(id: String, confirmar: Boolean, motivo: String) {
        val usuario = usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        val servicio = obtenerServicioPorId(id)
        require(usuario.rol != Rol.COORDINADOR && servicio.asignadoA == usuario.id) {
            "Solo el responsable puede responder este servicio."
        }
        require(!servicio.historico && servicio.estado == EstadoServicio.PENDIENTE) { "El servicio ya fue respondido." }
        require(confirmar || motivo.trim().length in 1..300) { "Escribe un motivo de rechazo de entre 1 y 300 caracteres." }
        check(almacen.responder(id, usuario.id,
            if (confirmar) EstadoServicio.CONFIRMADA else EstadoServicio.RECHAZADA,
            if (confirmar) null else motivo.trim(), java.time.Instant.now().toString())) {
            "El servicio ya cambió. Actualiza la lista."
        }
    }

    fun actualizarPerfil(nombre: String, correo: String): Usuario {
        val actual = usuarioActual ?: throw IllegalStateException("Debes iniciar sesión.")
        require(nombre.trim().length in 2..120) { "El nombre debe tener entre 2 y 120 caracteres." }
        require(correo.trim().length <= 254 && Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(correo.trim())) {
            "Ingresa un correo válido."
        }
        return actual.copy(nombre = nombre.trim(), correo = correo.trim()).also { usuarioActual = it }
    }

    fun cerrarSesion() {
        usuarioActual = null
    }
}
