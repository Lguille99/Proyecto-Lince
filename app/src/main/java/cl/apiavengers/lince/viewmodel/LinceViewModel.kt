package cl.apiavengers.lince.viewmodel

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.apiavengers.lince.datos.ArchivoEstado
import cl.apiavengers.lince.datos.ClienteSupabase
import cl.apiavengers.lince.datos.ErrorSupabase
import cl.apiavengers.lince.datos.PanelLince
import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.datos.SesionSupabase
import cl.apiavengers.lince.datos.SupabaseConfig
import cl.apiavengers.lince.datos.local.AlmacenEstadoRoom
import cl.apiavengers.lince.datos.local.LinceDatabase
import cl.apiavengers.lince.modelo.Disponibilidad
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.modelo.LinceUiState
import cl.apiavengers.lince.modelo.ResumenCoordinador
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LinceViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LinceUiState())
    val uiState = _uiState.asStateFlow()

    private var remoto: ClienteSupabase? = null
    private var repositorioLocal: RepositorioLince? = null
    private var sesion = 0

    fun iniciarSesion(cuenta: String, clave: String) {
        if (_uiState.value.cargando) return
        remoto?.cerrarSesion()
        remoto = null
        SesionSupabase.cliente = null
        repositorioLocal?.cerrarSesion()
        _uiState.value = LinceUiState(cargando = true)
        val token = ++sesion

        viewModelScope.launch {
            var errorRemoto: String? = null
            if (SupabaseConfig.disponible && hayRed()) {
                val cliente = ClienteSupabase()
                try {
                    val usuarioRemoto = cliente.iniciarSesion(cuenta, clave)
                    val panel = cliente.cargarPanel()
                    remoto = cliente
                    SesionSupabase.cliente = cliente
                    if (token == sesion) {
                        _uiState.value = estadoRemoto(usuarioRemoto, panel)
                    }
                    return@launch
                } catch (cancelacion: CancellationException) {
                    throw cancelacion
                } catch (problema: ErrorSupabase) {
                    cliente.cerrarSesion()
                    errorRemoto = problema.message
                } catch (problema: Exception) {
                    cliente.cerrarSesion()
                    errorRemoto = "Supabase no está disponible en este momento."
                }
            }

            var errorLocal: String? = null
            val estadoIniciado = try {
                val repositorio = obtenerRepositorio()
                val perfil = withContext(Dispatchers.IO) {
                    repositorio.iniciarSesion(cuenta, clave)
                }
                withContext(Dispatchers.IO) { estadoLocal(repositorio, perfil) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (problema: IllegalArgumentException) {
                errorLocal = problema.message
                null
            }

            if (token != sesion) return@launch
            _uiState.value = estadoIniciado?.copy(
                mensaje = errorRemoto?.let { "Modo local: $it" }
            ) ?: LinceUiState(
                error = errorLocal ?: errorRemoto ?: "Usuario o contraseña incorrectos."
            )
        }
    }

    fun cerrarSesion() {
        sesion++
        remoto?.cerrarSesion()
        remoto = null
        SesionSupabase.cliente = null
        repositorioLocal?.cerrarSesion()
        _uiState.value = LinceUiState()
    }

    fun esCoordinador(): Boolean = _uiState.value.usuario?.rol == Rol.COORDINADOR

    fun esTitular(servicio: Servicio): Boolean =
        _uiState.value.usuario?.let { servicio.esTitular(it.id) } == true

    fun sincronizar() {
        if (!esCoordinador() || _uiState.value.cargando) return
        val cliente = remoto
        if (cliente == null) {
            accionLocal(
                exito = "Resumen actualizado con los datos locales.",
                ultimaSincronizacion = LocalDateTime.now().format(HORA)
            ) { _, usuario -> usuario }
            return
        }
        val token = sesion
        _uiState.update { it.copy(cargando = true, error = null, mensaje = null, esError = false) }
        viewModelScope.launch {
            try {
                cliente.simularSincronizacion()
                val panel = cliente.cargarPanel()
                val usuario = cliente.usuario ?: _uiState.value.usuario
                    ?: throw ErrorSupabase("Debes iniciar sesión nuevamente.")
                if (token == sesion) {
                    _uiState.value = estadoRemoto(
                        usuario = usuario,
                        panel = panel,
                        mensaje = "Sincronización simulada en Supabase.",
                        ultimaSincronizacion = LocalDateTime.now().format(HORA)
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (problema: Exception) {
                if (token == sesion) {
                    _uiState.update {
                        it.copy(cargando = false, esError = true, mensaje = mensajeError(problema))
                    }
                }
            }
        }
    }

    fun confirmar(servicioId: String) {
        if (remoto == null) {
            accionLocal("Asignación confirmada.") { repositorio, usuario ->
                repositorio.confirmarServicio(servicioId)
                usuario
            }
        } else {
            accionRemota("Asignación confirmada.") { cliente ->
                cliente.responder(servicioId, true, null)
            }
        }
    }

    fun rechazar(servicioId: String, motivo: String) {
        if (remoto == null) {
            accionLocal("Asignación rechazada.") { repositorio, usuario ->
                repositorio.rechazarServicio(servicioId, motivo)
                usuario
            }
        } else {
            accionRemota("Asignación rechazada.") { cliente ->
                cliente.responder(servicioId, false, motivo)
            }
        }
    }

    fun registrarCheckIn(servicioId: String, ubicacion: String, observaciones: String = "") {
        if (remoto == null) {
            accionLocal("Check-in registrado.") { repositorio, usuario ->
                repositorio.registrarCheckIn(servicioId, ubicacion, observaciones)
                usuario
            }
        } else {
            accionRemota("Check-in registrado.") { cliente ->
                cliente.registrarCheckIn(servicioId, ubicacion, observaciones)
            }
        }
    }

    fun registrarCheckOut(servicioId: String, observaciones: String = "") {
        if (remoto == null) {
            accionLocal("Servicio finalizado.") { repositorio, usuario ->
                repositorio.registrarCheckOut(servicioId, observaciones)
                usuario
            }
        } else {
            accionRemota("Servicio finalizado.") { cliente ->
                cliente.registrarCheckOut(servicioId, observaciones)
            }
        }
    }

    fun calificar(servicioId: String, puntaje: Int) {
        if (remoto == null) {
            accionLocal("Calificación guardada.") { repositorio, usuario ->
                repositorio.calificarServicio(servicioId, puntaje)
                usuario
            }
        } else {
            accionRemota("Calificación guardada.") { cliente ->
                cliente.calificar(servicioId, puntaje)
            }
        }
    }

    fun reasignar(servicioId: String, nuevoUsuarioId: String) {
        if (remoto == null) {
            accionLocal("Servicio reasignado.") { repositorio, usuario ->
                repositorio.reasignarServicio(servicioId, nuevoUsuarioId)
                usuario
            }
        } else {
            accionRemota("Servicio reasignado.") { cliente ->
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
        if (remoto == null) {
            accionLocal("Perfil actualizado.") { repositorio, _ ->
                repositorio.actualizarPerfil(nombre, correo, telefono, zona, idiomas)
            }
        } else {
            accionRemota("Perfil actualizado.") { cliente ->
                cliente.actualizarPerfil(nombre, correo, telefono, zona, idiomas)
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun obtenerPersona(id: String): Usuario? = _uiState.value.equipo.find { it.id == id }

    fun obtenerServicio(id: String): Servicio? =
        (_uiState.value.servicios + _uiState.value.historial).find { it.id == id }

    fun serviciosDe(usuarioId: String): List<Servicio> =
        _uiState.value.servicios.filter { it.esDe(usuarioId) }

    fun serviciosDeHoy(): List<Servicio> =
        _uiState.value.servicios.filter { it.fecha == LocalDate.now().toString() }

    fun candidatosReasignacion(servicioId: String): List<Usuario> {
        val servicio = obtenerServicio(servicioId) ?: return emptyList()
        val actual = _uiState.value.equipo.find { it.id == servicio.asignadoA } ?: return emptyList()
        return _uiState.value.equipo.filter {
            it.rol == actual.rol &&
                it.id != servicio.asignadoA &&
                it.id != servicio.acompananteId &&
                servicio.idiomaRequerido in it.idiomas
        }
    }

    fun totalGanado(usuarioId: String): Int = (_uiState.value.servicios + _uiState.value.historial)
        .filter {
            it.esDe(usuarioId) &&
                (it.estado == EstadoServicio.CONFIRMADA || it.estado == EstadoServicio.COMPLETADA)
        }
        .sumOf { it.tarifa }

    fun promedioCalificacion(usuarioId: String): Double? {
        val puntajes = (_uiState.value.servicios + _uiState.value.historial)
            .filter { it.esDe(usuarioId) }
            .mapNotNull { it.puntaje }
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

    private suspend fun obtenerRepositorio(): RepositorioLince {
        repositorioLocal?.let { return it }
        val repositorio = withContext(Dispatchers.IO) {
            RepositorioLince(
                AlmacenEstadoRoom(
                    LinceDatabase.obtener(getApplication()).estadoDao(),
                    ArchivoEstado(getApplication())
                )
            )
        }
        repositorioLocal = repositorio
        return repositorio
    }

    private fun estadoLocal(
        repositorio: RepositorioLince,
        usuario: Usuario,
        mensaje: String? = null,
        esError: Boolean = false,
        ultimaSincronizacion: String? = _uiState.value.ultimaSincronizacion
    ): LinceUiState = LinceUiState(
        usuario = usuario,
        servicios = repositorio.consultarServicios(),
        historial = repositorio.consultarHistorial(),
        equipo = repositorio.consultarEquipo(),
        resumen = if (usuario.rol == Rol.COORDINADOR) repositorio.resumenCoordinador() else null,
        mensaje = mensaje,
        esError = esError,
        ultimaSincronizacion = ultimaSincronizacion,
        modoRemoto = false
    )

    private fun estadoRemoto(
        usuario: Usuario,
        panel: PanelLince,
        mensaje: String? = null,
        esError: Boolean = false,
        ultimaSincronizacion: String? = _uiState.value.ultimaSincronizacion
    ): LinceUiState {
        val activos = panel.servicios.filter {
            it.estado == EstadoServicio.PENDIENTE || it.estado == EstadoServicio.CONFIRMADA
        }
        val historial = panel.servicios.filter {
            it.estado == EstadoServicio.COMPLETADA || it.estado == EstadoServicio.RECHAZADA
        }
        return LinceUiState(
            usuario = usuario,
            servicios = activos,
            historial = historial,
            equipo = panel.equipo,
            resumen = if (usuario.rol == Rol.COORDINADOR) {
                resumenDesde(panel.servicios, panel.equipo)
            } else {
                null
            },
            mensaje = mensaje,
            esError = esError,
            ultimaSincronizacion = ultimaSincronizacion,
            modoRemoto = true
        )
    }

    private fun accionLocal(
        exito: String,
        ultimaSincronizacion: String? = null,
        bloque: (RepositorioLince, Usuario) -> Usuario
    ) {
        val actual = _uiState.value
        if (actual.cargando) return
        val usuario = actual.usuario ?: return
        val token = sesion
        _uiState.update {
            it.copy(cargando = true, error = null, mensaje = null, esError = false)
        }
        viewModelScope.launch {
            try {
                val repositorio = obtenerRepositorio()
                val actualizado = withContext(Dispatchers.IO) {
                    bloque(repositorio, usuario)
                }
                val estado = withContext(Dispatchers.IO) {
                    estadoLocal(
                        repositorio = repositorio,
                        usuario = actualizado,
                        mensaje = exito,
                        ultimaSincronizacion = ultimaSincronizacion
                    )
                }
                if (token == sesion) _uiState.value = estado
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (problema: Exception) {
                if (token == sesion) {
                    _uiState.update {
                        it.copy(cargando = false, esError = true, mensaje = mensajeError(problema))
                    }
                }
            }
        }
    }

    private fun accionRemota(
        exito: String,
        bloque: suspend (ClienteSupabase) -> Unit
    ) {
        val cliente = remoto ?: return
        if (_uiState.value.cargando) return
        val token = sesion
        _uiState.update {
            it.copy(cargando = true, error = null, mensaje = null, esError = false)
        }
        viewModelScope.launch {
            try {
                bloque(cliente)
                val panel = cliente.cargarPanel()
                val usuario = cliente.usuario ?: _uiState.value.usuario
                    ?: throw ErrorSupabase("Debes iniciar sesión nuevamente.")
                if (token == sesion) {
                    _uiState.value = estadoRemoto(usuario, panel, mensaje = exito)
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (problema: Exception) {
                if (token == sesion) {
                    _uiState.update {
                        it.copy(cargando = false, esError = true, mensaje = mensajeError(problema))
                    }
                }
            }
        }
    }

    private fun hayRed(): Boolean {
        val gestor = getApplication<Application>()
            .getSystemService(ConnectivityManager::class.java) ?: return true
        val red = gestor.activeNetwork ?: return false
        val capacidades = gestor.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun mensajeError(error: Exception): String = when {
        error is IllegalArgumentException || error is IllegalStateException -> {
            error.message ?: "No se pudo completar la operación."
        }
        error is ErrorSupabase -> error.message ?: "No se pudo completar la operación."
        else -> "No se pudo acceder a los datos. Intenta nuevamente."
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
