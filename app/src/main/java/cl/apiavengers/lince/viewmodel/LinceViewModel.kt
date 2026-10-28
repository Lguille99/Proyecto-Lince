package cl.apiavengers.lince.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.apiavengers.lince.datos.ArchivoEstado
import cl.apiavengers.lince.datos.ClienteSupabase
import cl.apiavengers.lince.datos.ErrorSupabase
import cl.apiavengers.lince.datos.PanelLince
import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.datos.SesionSupabase
import cl.apiavengers.lince.datos.SupabaseConfig
import cl.apiavengers.lince.modelo.Disponibilidad
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.modelo.ResumenCoordinador
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LinceViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = RepositorioLince(ArchivoEstado(application))
    private var remoto: ClienteSupabase? = null

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
    var cargando by mutableStateOf(false)
        private set
    var modoRemoto by mutableStateOf(false)
        private set

    fun iniciarSesion(cuenta: String, clave: String) {
        if (cargando) return
        limpiarSesion()
        error = null
        mensaje = null
        cargando = true
        viewModelScope.launch {
            var errorRemoto: String? = null
            if (SupabaseConfig.disponible) {
                val cliente = ClienteSupabase()
                try {
                    val usuarioRemoto = cliente.iniciarSesion(cuenta, clave)
                    remoto = cliente
                    SesionSupabase.cliente = cliente
                    usuario = usuarioRemoto
                    modoRemoto = true
                    refrescarRemoto()
                    cargando = false
                    return@launch
                } catch (cancelacion: CancellationException) {
                    throw cancelacion
                } catch (problema: ErrorSupabase) {
                    cliente.cerrarSesion()
                    remoto = null
                    SesionSupabase.cliente = null
                    usuario = null
                    modoRemoto = false
                    errorRemoto = problema.message
                } catch (problema: Exception) {
                    cliente.cerrarSesion()
                    remoto = null
                    SesionSupabase.cliente = null
                    usuario = null
                    modoRemoto = false
                    errorRemoto = "Supabase no está disponible en este momento."
                }
            }
            val local = iniciarSesionLocal(cuenta, clave)
            if (!local) {
                if (errorRemoto != null) error = errorRemoto
                else if (error == null) error = "Usuario o contraseña incorrectos."
            } else if (errorRemoto != null) {
                mensaje = "Modo local: $errorRemoto"
                esError = false
            }
            cargando = false
        }
    }

    fun cerrarSesion() {
        remoto?.cerrarSesion()
        remoto = null
        SesionSupabase.cliente = null
        repositorio.cerrarSesion()
        limpiarSesion()
    }

    fun esCoordinador(): Boolean = usuario?.rol == Rol.COORDINADOR

    fun esTitular(servicio: Servicio): Boolean = usuario?.let { servicio.esTitular(it.id) } == true

    fun sincronizar() {
        val coordinador = usuario?.rol == Rol.COORDINADOR
        if (!coordinador || cargando) return
        val cliente = remoto
        if (cliente == null) {
            refrescarLocal()
            ultimaSincronizacion = LocalDateTime.now().format(HORA)
            mensaje = "Resumen actualizado con los datos locales."
            esError = false
            return
        }
        cargando = true
        viewModelScope.launch {
            try {
                cliente.simularSincronizacion()
                refrescarRemoto()
                ultimaSincronizacion = LocalDateTime.now().format(HORA)
                mensaje = "Sincronización simulada en Supabase."
                esError = false
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (problema: Exception) {
                esError = true
                mensaje = problema.message ?: "No se pudo sincronizar."
            } finally {
                cargando = false
            }
        }
    }

    fun confirmar(servicioId: String) {
        val cliente = remoto
        if (cliente == null) {
            accionLocal("Asignación confirmada.") { repositorio.confirmarServicio(servicioId) }
        } else {
            accionRemota("Asignación confirmada.") { cliente.responder(servicioId, true, null) }
        }
    }

    fun rechazar(servicioId: String, motivo: String) {
        val cliente = remoto
        if (cliente == null) {
            accionLocal("Asignación rechazada.") { repositorio.rechazarServicio(servicioId, motivo) }
        } else {
            accionRemota("Asignación rechazada.") { cliente.responder(servicioId, false, motivo) }
        }
    }

    fun registrarCheckIn(servicioId: String, ubicacion: String, observaciones: String = "") {
        val cliente = remoto
        if (cliente == null) {
            accionLocal("Check-in registrado.") {
                repositorio.registrarCheckIn(servicioId, ubicacion, observaciones)
            }
        } else {
            accionRemota("Check-in registrado.") {
                cliente.registrarCheckIn(servicioId, ubicacion, observaciones)
            }
        }
    }

    fun registrarCheckOut(servicioId: String, observaciones: String = "") {
        val cliente = remoto
        if (cliente == null) {
            accionLocal("Servicio finalizado.") {
                repositorio.registrarCheckOut(servicioId, observaciones)
            }
        } else {
            accionRemota("Servicio finalizado.") {
                cliente.registrarCheckOut(servicioId, observaciones)
            }
        }
    }

    fun calificar(servicioId: String, puntaje: Int) {
        val cliente = remoto
        if (cliente == null) {
            accionLocal("Calificación guardada.") { repositorio.calificarServicio(servicioId, puntaje) }
        } else {
            accionRemota("Calificación guardada.") { cliente.calificar(servicioId, puntaje) }
        }
    }

    fun reasignar(servicioId: String, nuevoUsuarioId: String) {
        val cliente = remoto
        if (cliente == null) {
            accionLocal("Servicio reasignado.") {
                repositorio.reasignarServicio(servicioId, nuevoUsuarioId)
            }
        } else {
            accionRemota("Servicio reasignado.") {
                cliente.reasignar(servicioId, nuevoUsuarioId)
            }
        }
    }

    fun guardarPerfil(
        nombre: String,
        correo: String,
        telefono: String,
        zona: String,
        idiomas: List<Idioma>
    ) {
        val cliente = remoto
        if (cliente == null) {
            accionLocal("Perfil actualizado.") {
                usuario = repositorio.actualizarPerfil(nombre, correo, telefono, zona, idiomas)
            }
        } else {
            accionRemota("Perfil actualizado.") {
                cliente.actualizarPerfil(nombre, correo, telefono, zona, idiomas)
                usuario = cliente.usuario
            }
        }
    }

    fun limpiarMensaje() {
        mensaje = null
    }

    fun obtenerPersona(id: String): Usuario? = equipo.find { it.id == id }

    fun obtenerServicio(id: String): Servicio? =
        (servicios + historial).find { it.id == id }

    fun serviciosDe(usuarioId: String): List<Servicio> =
        servicios.filter { it.esDe(usuarioId) }

    fun serviciosDeHoy(): List<Servicio> =
        servicios.filter { it.fecha == LocalDate.now().toString() }

    fun candidatosReasignacion(servicioId: String): List<Usuario> {
        val servicio = obtenerServicio(servicioId) ?: return emptyList()
        val actual = equipo.find { it.id == servicio.asignadoA } ?: return emptyList()
        return equipo.filter {
            it.rol == actual.rol &&
                it.id != servicio.asignadoA &&
                it.id != servicio.acompananteId &&
                servicio.idiomaRequerido in it.idiomas
        }
    }

    fun totalGanado(usuarioId: String): Int = (servicios + historial)
        .filter {
            it.esDe(usuarioId) &&
                (it.estado == EstadoServicio.CONFIRMADA || it.estado == EstadoServicio.COMPLETADA)
        }
        .sumOf { it.tarifa }

    fun promedioCalificacion(usuarioId: String): Double? {
        val puntajes = (servicios + historial).filter { it.esDe(usuarioId) }.mapNotNull { it.puntaje }
        if (puntajes.isEmpty()) return null
        return puntajes.average()
    }

    fun puedeResponder(servicio: Servicio): Boolean =
        esTitular(servicio) && servicio.estado == EstadoServicio.PENDIENTE

    fun puedeIniciar(servicio: Servicio): Boolean =
        esTitular(servicio) &&
            servicio.estado == EstadoServicio.CONFIRMADA &&
            servicio.inicioEn == null &&
            !esCoordinador()

    fun esHoy(servicio: Servicio): Boolean = servicio.fecha == LocalDate.now().toString()

    private suspend fun refrescarRemoto() {
        val cliente = remoto ?: throw ErrorSupabase("Debes iniciar sesión nuevamente.")
        val panel = cliente.cargarPanel()
        aplicarPanel(panel)
    }

    private fun aplicarPanel(panel: PanelLince) {
        servicios = panel.servicios.filter {
            it.estado == EstadoServicio.PENDIENTE || it.estado == EstadoServicio.CONFIRMADA
        }
        historial = panel.servicios.filter {
            it.estado == EstadoServicio.COMPLETADA || it.estado == EstadoServicio.RECHAZADA
        }
        equipo = panel.equipo
        resumen = if (usuario?.rol == Rol.COORDINADOR) {
            resumenDesde(panel.servicios, panel.equipo)
        } else {
            null
        }
    }

    private fun refrescarLocal() {
        servicios = repositorio.consultarServicios()
        historial = repositorio.consultarHistorial()
        equipo = repositorio.consultarEquipo()
        resumen = if (usuario?.rol == Rol.COORDINADOR) repositorio.resumenCoordinador() else null
    }

    private fun iniciarSesionLocal(cuenta: String, clave: String): Boolean = try {
        usuario = repositorio.iniciarSesion(cuenta, clave)
        modoRemoto = false
        refrescarLocal()
        true
    } catch (problema: IllegalArgumentException) {
        usuario = null
        error = problema.message
        false
    }

    private fun accionLocal(exito: String, bloque: () -> Unit) {
        try {
            bloque()
            refrescarLocal()
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

    private fun accionRemota(exito: String, bloque: suspend () -> Unit) {
        if (cargando) return
        cargando = true
        viewModelScope.launch {
            try {
                bloque()
                refrescarRemoto()
                esError = false
                mensaje = exito
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (problema: Exception) {
                esError = true
                mensaje = problema.message ?: "No se pudo completar la operación."
            } finally {
                cargando = false
            }
        }
    }

    private fun limpiarSesion() {
        remoto?.cerrarSesion()
        remoto = null
        SesionSupabase.cliente = null
        usuario = null
        servicios = emptyList()
        historial = emptyList()
        equipo = emptyList()
        resumen = null
        error = null
        mensaje = null
        esError = false
        ultimaSincronizacion = null
        modoRemoto = false
    }

    private fun resumenDesde(servicios: List<Servicio>, equipo: List<Usuario>): ResumenCoordinador {
        val activos = servicios.filter {
            it.estado == EstadoServicio.PENDIENTE || it.estado == EstadoServicio.CONFIRMADA
        }
        val historial = servicios.filter {
            it.estado == EstadoServicio.COMPLETADA || it.estado == EstadoServicio.RECHAZADA
        }
        return ResumenCoordinador(
            serviciosActivos = activos.size,
            pendientes = activos.count { it.estado == EstadoServicio.PENDIENTE },
            confirmadas = activos.count { it.estado == EstadoServicio.CONFIRMADA },
            rechazadas = historial.count { it.estado == EstadoServicio.RECHAZADA },
            enEjecucion = activos.count { it.inicioEn != null && it.terminoEn == null },
            personasEquipo = equipo.size,
            personasDisponibles = equipo.count { it.disponibilidad == Disponibilidad.DISPONIBLE },
            historialTotal = historial.size
        )
    }

    private companion object {
        val HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    }
}
