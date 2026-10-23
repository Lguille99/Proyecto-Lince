package cl.apiavengers.lince.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.datos.local.AlmacenServiciosRoom
import cl.apiavengers.lince.datos.local.LinceDatabase
import cl.apiavengers.lince.modelo.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LinceViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = RepositorioLince(AlmacenServiciosRoom(LinceDatabase.obtener(application).servicioDao()))
    private val _uiState = MutableStateFlow(LinceUiState())
    val uiState = _uiState.asStateFlow()
    private var tarea: Job? = null
    private var sesion = 0

    private fun cargar(perfil: Usuario): LinceUiState = LinceUiState(
        usuario = perfil,
        servicios = repositorio.consultarServicios(),
        historial = repositorio.consultarHistorial(),
        equipo = repositorio.consultarEquipo().map { if (it.id == perfil.id) perfil else it },
        resumen = if (perfil.rol == Rol.COORDINADOR) repositorio.resumenCoordinador() else null
    )

    fun iniciarSesion(cuenta: String, clave: String) {
        if (_uiState.value.cargando) return
        _uiState.value = LinceUiState(cargando = true)
        tarea = viewModelScope.launch {
            try {
                val estado = withContext(Dispatchers.IO) { cargar(repositorio.iniciarSesion(cuenta, clave)) }
                _uiState.value = estado
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.value = LinceUiState(error = mensajeError(error))
            }
        }
    }

    private fun ejecutar(mensaje: String, accion: () -> Usuario) {
        if (_uiState.value.cargando || _uiState.value.usuario == null) return
        val actual = sesion
        _uiState.update { it.copy(cargando = true, error = null, mensaje = null) }
        tarea = viewModelScope.launch {
            try {
                val estado = withContext(Dispatchers.IO) { cargar(accion()) }
                if (actual == sesion) _uiState.value = estado.copy(mensaje = mensaje)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                if (actual == sesion) _uiState.update { it.copy(cargando = false, error = mensajeError(error)) }
            }
        }
    }

    fun responder(id: String, confirmar: Boolean, motivo: String) {
        val perfil = _uiState.value.usuario ?: return
        ejecutar(if (confirmar) "Servicio confirmado y guardado." else "Rechazo guardado.") {
            repositorio.responderServicio(id, confirmar, motivo)
            perfil
        }
    }

    fun actualizarUsuario(nombre: String, correo: String) {
        ejecutar("Perfil actualizado solo durante esta sesión.") { repositorio.actualizarPerfil(nombre, correo) }
    }

    fun sincronizar() {
        val perfil = _uiState.value.usuario ?: return
        ejecutar("Datos locales actualizados. Sin conexión al sistema del cliente.") { perfil }
    }

    fun limpiarMensaje() { _uiState.update { it.copy(error = null, mensaje = null) } }

    fun cerrarSesion() {
        sesion++
        tarea?.cancel()
        repositorio.cerrarSesion()
        _uiState.value = LinceUiState()
    }

    private fun mensajeError(error: Exception): String =
        if (error is IllegalArgumentException || error is IllegalStateException)
            error.message ?: "No se pudo completar la operación."
        else "No se pudo acceder a los datos locales. Intenta nuevamente."
}
