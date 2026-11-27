package cl.apiavengers.lince.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.apiavengers.lince.datos.ArchivoEstado
import cl.apiavengers.lince.datos.MonitorConexion
import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.datos.local.AlmacenEstadoRoom
import cl.apiavengers.lince.datos.local.ColaSincronizacion
import cl.apiavengers.lince.datos.local.LinceDatabase
import cl.apiavengers.lince.datos.local.OperacionPendiente
import cl.apiavengers.lince.datos.local.SincronizadorLocal
import cl.apiavengers.lince.datos.local.TipoOperacionSync
import cl.apiavengers.lince.datos.preferencias.Preferencias
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.modelo.LinceUiState
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Ubicacion
import cl.apiavengers.lince.modelo.Usuario
import cl.apiavengers.lince.ui.idioma.PreferenciaIdioma
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LinceViewModel(
    application: Application,
    private val monitorConexion: MonitorConexion = MonitorConexion(application),
    private val preferencias: cl.apiavengers.lince.datos.preferencias.PreferenciasDataStore = Preferencias.obtener(application),
    private val cola: ColaSincronizacion = ColaSincronizacion(LinceDatabase.obtener(application).operacionPendienteDao()),
    private val repositorioProvider: () -> RepositorioLince = {
        RepositorioLince(
            AlmacenEstadoRoom(
                LinceDatabase.obtener(application).estadoDao(),
                ArchivoEstado(application)
            )
        )
    },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {
    constructor(application: Application) : this(
        application,
        MonitorConexion(application),
        Preferencias.obtener(application),
        ColaSincronizacion(LinceDatabase.obtener(application).operacionPendienteDao()),
        {
            RepositorioLince(
                AlmacenEstadoRoom(
                    LinceDatabase.obtener(application).estadoDao(),
                    ArchivoEstado(application)
                )
            )
        },
        Dispatchers.IO
    )

    private val _uiState = MutableStateFlow(LinceUiState())
    val uiState = _uiState.asStateFlow()

    private val prefs = preferencias
    private var repositorioLocal: RepositorioLince? = null
    private var sesion = 0

    private val t get() = PreferenciaIdioma.actuales()

    init {
        viewModelScope.launch {
            prefs.ultimaSincronizacion.collect { valor ->
                if (valor != null) _uiState.update { it.copy(ultimaSincronizacion = valor) }
            }
        }
        viewModelScope.launch {
            monitorConexion.conectado.collect { conectado ->
                _uiState.update { it.copy(conectado = conectado) }
                if (conectado) sincronizarPendientes()
            }
        }
        viewModelScope.launch {
            withContext(ioDispatcher) { cola.actualizarPendientes() }
            ColaSincronizacion.pendientes.collect { pendientes ->
                _uiState.update { it.copy(pendientesSincronizar = pendientes) }
                if (pendientes > 0 && _uiState.value.conectado) sincronizarPendientes()
            }
        }
    }

    fun iniciarSesion(cuenta: String, clave: String) {
        if (_uiState.value.cargando) return
        repositorioLocal?.cerrarSesion()
        val base = _uiState.value
        _uiState.value = LinceUiState(
            cargando = true,
            conectado = base.conectado,
            pendientesSincronizar = base.pendientesSincronizar,
            ultimaSincronizacion = base.ultimaSincronizacion,
            sincronizando = base.sincronizando
        )
        val token = ++sesion

        viewModelScope.launch {
            var errorLocal: String? = null
            val estadoIniciado = try {
                val repositorio = obtenerRepositorio()
                val perfil = withContext(ioDispatcher) {
                    repositorio.iniciarSesion(cuenta, clave)
                }
                withContext(ioDispatcher) { estadoLocal(repositorio, perfil) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (problema: IllegalArgumentException) {
                errorLocal = PreferenciaIdioma.traducir(problema.message)
                null
            }

            if (token != sesion) return@launch
            _uiState.value = estadoIniciado ?: LinceUiState(
                error = errorLocal ?: t.msgIncorrectCredentials,
                conectado = base.conectado,
                pendientesSincronizar = base.pendientesSincronizar,
                ultimaSincronizacion = base.ultimaSincronizacion
            )
        }
    }

    fun cerrarSesion() {
        sesion++
        repositorioLocal?.cerrarSesion()
        val base = _uiState.value
        _uiState.value = LinceUiState(
            conectado = base.conectado,
            pendientesSincronizar = base.pendientesSincronizar,
            ultimaSincronizacion = base.ultimaSincronizacion,
            sincronizando = base.sincronizando
        )
    }

    fun esCoordinador(): Boolean = _uiState.value.usuario?.rol == Rol.COORDINADOR

    fun esTitular(servicio: Servicio): Boolean =
        _uiState.value.usuario?.let { servicio.esTitular(it.id) } == true

    fun sincronizar() {
        if (_uiState.value.cargando || _uiState.value.sincronizando) return
        if (!_uiState.value.conectado) {
            _uiState.update { it.copy(mensaje = t.msgSyncFailed, esError = true) }
            return
        }
        sincronizarPendientes(mostrarMensaje = true)
    }

    fun confirmar(servicioId: String) {
        accionLocal(
            exito = t.msgAssignmentConfirmed,
            operacion = OperacionPendiente(
                TipoOperacionSync.CONFIRMAR,
                servicioId,
                "Confirmar asignación $servicioId"
            )
        ) { repositorio, usuario ->
            repositorio.confirmarServicio(servicioId)
            usuario
        }
    }

    fun rechazar(servicioId: String, motivo: String) {
        accionLocal(
            exito = t.msgAssignmentRejected,
            operacion = OperacionPendiente(
                TipoOperacionSync.RECHAZAR,
                servicioId,
                "Rechazar $servicioId · $motivo"
            )
        ) { repositorio, usuario ->
            repositorio.rechazarServicio(servicioId, motivo)
            usuario
        }
    }

    fun registrarCheckIn(
        servicioId: String,
        ubicacion: String,
        observaciones: String = "",
        posicion: Ubicacion? = null
    ) {
        accionLocal(
            exito = t.msgCheckinRegistered,
            operacion = OperacionPendiente(
                TipoOperacionSync.CHECK_IN,
                servicioId,
                "Check-in $servicioId · $ubicacion"
            )
        ) { repositorio, usuario ->
            repositorio.registrarCheckIn(servicioId, ubicacion, observaciones, posicion)
            usuario
        }
    }

    fun registrarCheckOut(
        servicioId: String,
        observaciones: String = "",
        posicion: Ubicacion? = null,
        ubicacionTermino: String = ""
    ) {
        accionLocal(
            exito = t.msgServiceFinished,
            operacion = OperacionPendiente(
                TipoOperacionSync.CHECK_OUT,
                servicioId,
                "Check-out $servicioId · $ubicacionTermino"
            )
        ) { repositorio, usuario ->
            repositorio.registrarCheckOut(servicioId, observaciones, posicion, ubicacionTermino)
            usuario
        }
    }

    fun reportarUbicacion(servicioId: String, posicion: Ubicacion) {
        accionLocal(
            exito = t.msgLocationReported,
            operacion = OperacionPendiente(
                TipoOperacionSync.REPORTAR_UBICACION,
                servicioId,
                "Ubicación $servicioId · ${posicion.latitud}, ${posicion.longitud}"
            )
        ) { repositorio, usuario ->
            repositorio.reportarUbicacion(servicioId, posicion)
            usuario
        }
    }

    fun calificar(servicioId: String, puntaje: Int) {
        accionLocal(
            exito = t.msgRatingSaved,
            operacion = OperacionPendiente(
                TipoOperacionSync.CALIFICAR,
                servicioId,
                "Calificar $servicioId · $puntaje/5"
            )
        ) { repositorio, usuario ->
            repositorio.calificarServicio(servicioId, puntaje)
            usuario
        }
    }

    fun reasignar(servicioId: String, nuevoUsuarioId: String) {
        accionLocal(
            exito = t.msgServiceReassigned,
            operacion = OperacionPendiente(
                TipoOperacionSync.REASIGNAR,
                servicioId,
                "Reasignar $servicioId a $nuevoUsuarioId"
            )
        ) { repositorio, usuario ->
            repositorio.reasignarServicio(servicioId, nuevoUsuarioId)
            usuario
        }
    }

    fun guardarPerfil(
        nombre: String,
        correo: String,
        telefono: String,
        zona: String,
        idiomas: List<Idioma>
    ) {
        accionLocal(
            exito = t.msgProfileUpdated,
            operacion = OperacionPendiente(
                TipoOperacionSync.PERFIL,
                null,
                "Perfil · $nombre · $correo"
            )
        ) { repositorio, _ ->
            repositorio.actualizarPerfil(nombre, correo, telefono, zona, idiomas)
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
        val repositorio = withContext(ioDispatcher) { repositorioProvider() }
        repositorioLocal = repositorio
        return repositorio
    }

    private fun estadoLocal(
        repositorio: RepositorioLince,
        usuario: Usuario,
        mensaje: String? = null,
        esError: Boolean = false,
        ultimaSincronizacion: String? = _uiState.value.ultimaSincronizacion,
        pendientes: Int = _uiState.value.pendientesSincronizar,
        conectado: Boolean = _uiState.value.conectado
    ): LinceUiState = LinceUiState(
        usuario = usuario,
        servicios = repositorio.consultarServicios(),
        historial = repositorio.consultarHistorial(),
        equipo = repositorio.consultarEquipo(),
        resumen = if (usuario.rol == Rol.COORDINADOR) repositorio.resumenCoordinador() else null,
        mensaje = mensaje,
        esError = esError,
        ultimaSincronizacion = ultimaSincronizacion,
        conectado = conectado,
        pendientesSincronizar = pendientes,
        sincronizando = false
    )

    private fun accionLocal(
        exito: String,
        operacion: OperacionPendiente? = null,
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
                val actualizado = withContext(ioDispatcher) {
                    bloque(repositorio, usuario)
                }
                val pendientes = withContext(ioDispatcher) {
                    operacion?.let { cola.encolar(it) }
                    cola.contar()
                }
                val estado = withContext(ioDispatcher) {
                    estadoLocal(repositorio, actualizado, mensaje = exito, pendientes = pendientes)
                }
                if (token != sesion) return@launch
                _uiState.value = estado
                if (operacion != null && estado.conectado) sincronizarPendientes()
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

    private fun sincronizarPendientes(mostrarMensaje: Boolean = false) {
        if (_uiState.value.sincronizando) return
        val token = sesion
        _uiState.update { it.copy(sincronizando = true) }
        viewModelScope.launch {
            try {
                val pendientesAntes = withContext(ioDispatcher) { cola.contar() }
                val enviadas = if (pendientesAntes > 0) {
                    withContext(ioDispatcher) { cola.sincronizar(SincronizadorLocal()) }
                } else {
                    0
                }
                val ahora = LocalDateTime.now().format(HORA)
                val actualizarHora = pendientesAntes > 0 || mostrarMensaje
                if (actualizarHora) {
                    withContext(ioDispatcher) { prefs.fijarUltimaSincronizacion(ahora) }
                }
                val pendientes = withContext(ioDispatcher) {
                    cola.limpiarSincronizadas()
                    cola.contar()
                }
                if (token != sesion) return@launch
                _uiState.update {
                    it.copy(
                        sincronizando = false,
                        pendientesSincronizar = pendientes,
                        ultimaSincronizacion = if (actualizarHora) ahora else it.ultimaSincronizacion,
                        mensaje = if (mostrarMensaje) t.msgSyncCompleted(enviadas) else it.mensaje,
                        esError = if (mostrarMensaje) false else it.esError
                    )
                }
                if (pendientes > 0 && _uiState.value.conectado) sincronizarPendientes()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (problema: Exception) {
                if (token == sesion) {
                    _uiState.update {
                        it.copy(
                            sincronizando = false,
                            mensaje = if (mostrarMensaje) t.msgSyncFailed else it.mensaje,
                            esError = mostrarMensaje
                        )
                    }
                }
            }
        }
    }

    private fun mensajeError(error: Exception): String = when {
        error is IllegalArgumentException || error is IllegalStateException -> {
            PreferenciaIdioma.traducir(error.message) ?: t.msgOperationFailed
        }
        else -> t.msgDataAccessFailed
    }

    private companion object {
        val HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    }

}
