package cl.apiavengers.lince.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import cl.apiavengers.lince.datos.ArchivoEstado
import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.modelo.ResumenCoordinador
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LinceViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = RepositorioLince(ArchivoEstado(application))

    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var servicios by mutableStateOf<List<Servicio>>(emptyList())
        private set
    var historial by mutableStateOf<List<Servicio>>(emptyList())
        private set
    var equipo by mutableStateOf<List<Usuario>>(emptyList())
        private set
    var resumen by mutableStateOf<ResumenCoordinador?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var esError by mutableStateOf(false)
        private set
    var ultimaSincronizacion by mutableStateOf<String?>(null)
        private set

    fun iniciarSesion(cuenta: String, clave: String) {
        usuario = null
        servicios = emptyList()
        historial = emptyList()
        equipo = emptyList()
        resumen = null
        error = null
        mensaje = null
        ultimaSincronizacion = null
        try {
            usuario = repositorio.iniciarSesion(cuenta, clave)
            refrescar()
        } catch (problema: IllegalArgumentException) {
            error = problema.message
        }
    }

    fun cerrarSesion() {
        repositorio.cerrarSesion()
        usuario = null
        servicios = emptyList()
        historial = emptyList()
        equipo = emptyList()
        resumen = null
        error = null
        mensaje = null
        ultimaSincronizacion = null
    }

    fun esCoordinador(): Boolean = usuario?.rol == Rol.COORDINADOR

    fun esTitular(servicio: Servicio): Boolean = usuario?.let { servicio.esTitular(it.id) } == true

    fun sincronizar() {
        if (usuario?.rol == Rol.COORDINADOR) {
            refrescar()
            ultimaSincronizacion = LocalDateTime.now().format(HORA)
            mensaje = "Resumen actualizado con los datos de prueba."
            esError = false
        }
    }

    fun confirmar(servicioId: String) = accion("Asignación confirmada.") {
        repositorio.confirmarServicio(servicioId)
    }

    fun rechazar(servicioId: String, motivo: String) = accion("Asignación rechazada.") {
        repositorio.rechazarServicio(servicioId, motivo)
    }

    fun registrarCheckIn(servicioId: String, ubicacion: String, observaciones: String = "") =
        accion("Check-in registrado.") {
            repositorio.registrarCheckIn(servicioId, ubicacion, observaciones)
        }

    fun registrarCheckOut(servicioId: String, observaciones: String = "") =
        accion("Servicio finalizado.") {
            repositorio.registrarCheckOut(servicioId, observaciones)
        }

    fun calificar(servicioId: String, puntaje: Int) = accion("Calificación guardada.") {
        repositorio.calificarServicio(servicioId, puntaje)
    }

    fun reasignar(servicioId: String, nuevoUsuarioId: String) = accion("Servicio reasignado.") {
        repositorio.reasignarServicio(servicioId, nuevoUsuarioId)
    }

    fun guardarPerfil(
        nombre: String,
        correo: String,
        telefono: String,
        zona: String,
        idiomas: List<Idioma>
    ) = accion("Perfil actualizado.") {
        usuario = repositorio.actualizarPerfil(nombre, correo, telefono, zona, idiomas)
    }

    fun limpiarMensaje() {
        mensaje = null
    }

    fun obtenerPersona(id: String): Usuario? = try {
        repositorio.obtenerUsuarioPorId(id)
    } catch (_: Exception) {
        null
    }

    fun obtenerServicio(id: String): Servicio? = try {
        repositorio.obtenerServicioPorId(id)
    } catch (_: Exception) {
        null
    }

    fun serviciosDe(usuarioId: String): List<Servicio> = try {
        repositorio.consultarServiciosDe(usuarioId)
    } catch (_: Exception) {
        emptyList()
    }

    fun serviciosDeHoy(): List<Servicio> = try {
        repositorio.consultarAgendaDelDia()
    } catch (_: Exception) {
        emptyList()
    }

    fun candidatosReasignacion(servicioId: String): List<Usuario> = try {
        repositorio.candidatosReasignacion(servicioId)
    } catch (_: Exception) {
        emptyList()
    }

    fun totalGanado(usuarioId: String): Int = try {
        repositorio.totalGanado(usuarioId)
    } catch (_: Exception) {
        0
    }

    fun promedioCalificacion(usuarioId: String): Double? = try {
        repositorio.promedioCalificacion(usuarioId)
    } catch (_: Exception) {
        null
    }

    fun puedeResponder(servicio: Servicio): Boolean =
        esTitular(servicio) && servicio.estado == EstadoServicio.PENDIENTE

    fun puedeIniciar(servicio: Servicio): Boolean =
        esTitular(servicio) && servicio.estado == EstadoServicio.CONFIRMADA && !esCoordinador()

    fun esHoy(servicio: Servicio): Boolean = servicio.fecha == LocalDate.now().toString()

    private fun accion(exito: String, bloque: () -> Unit) {
        try {
            bloque()
            refrescar()
            esError = false
            mensaje = exito
        } catch (problema: IllegalArgumentException) {
            esError = true
            mensaje = problema.message
        } catch (problema: IllegalStateException) {
            esError = true
            mensaje = problema.message
        }
    }

    private fun refrescar() {
        servicios = repositorio.consultarServicios()
        historial = repositorio.consultarHistorial()
        equipo = repositorio.consultarEquipo()
        resumen = if (usuario?.rol == Rol.COORDINADOR) repositorio.resumenCoordinador() else null
    }

    private companion object {
        val HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    }
}
