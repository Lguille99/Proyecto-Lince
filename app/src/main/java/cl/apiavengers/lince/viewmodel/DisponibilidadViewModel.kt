package cl.apiavengers.lince.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.apiavengers.lince.datos.local.AlmacenDisponibilidadRoom
import cl.apiavengers.lince.datos.local.ColaSincronizacion
import cl.apiavengers.lince.datos.local.LinceDatabase
import cl.apiavengers.lince.datos.local.OperacionPendiente
import cl.apiavengers.lince.datos.local.TipoOperacionSync
import cl.apiavengers.lince.datos.ArchivoDisponibilidad
import cl.apiavengers.lince.datos.RepositorioDisponibilidad
import cl.apiavengers.lince.modelo.DisponibilidadUiState
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import cl.apiavengers.lince.ui.idioma.PreferenciaIdioma
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DisponibilidadViewModel(
    private val repositorio: RepositorioDisponibilidad,
    private val cola: ColaSincronizacion,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    constructor(application: Application) : this(
        RepositorioDisponibilidad(
            AlmacenDisponibilidadRoom(LinceDatabase.obtener(application).disponibilidadDao(), ArchivoDisponibilidad(application))
        ),
        ColaSincronizacion(LinceDatabase.obtener(application).operacionPendienteDao()),
        Dispatchers.IO
    )
    private val _uiState = MutableStateFlow(DisponibilidadUiState())
    val uiState = _uiState.asStateFlow()
    private var usuarioActual: Usuario? = null

    private val t get() = PreferenciaIdioma.actuales()

    fun cargar(usuario: Usuario) {
        usuarioActual = usuario
        _uiState.value = DisponibilidadUiState()
        viewModelScope.launch {
            try {
                val dias = withContext(ioDispatcher) { repositorio.consultar(usuario) }
                _uiState.update { it.copy(diasBloqueados = dias, bloqueado = it.fecha in dias, cargando = false, cargado = true) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update { it.copy(cargando = false, esError = true, mensaje = t.msgAgendaReadFailed) }
            }
        }
    }

    fun seleccionarFecha(fecha: LocalDate) {
        if (_uiState.value.cargando || fecha.isBefore(LocalDate.now())) return
        _uiState.update { it.copy(fecha = fecha, bloqueado = fecha in it.diasBloqueados, mensaje = null, esError = false) }
    }

    fun cambiarBloqueo(bloqueado: Boolean) {
        if (_uiState.value.cargando || !_uiState.value.cargado) return
        _uiState.update { it.copy(bloqueado = bloqueado, mensaje = null, esError = false) }
    }

    fun guardar(serviciosAsignados: List<Servicio> = emptyList()) {
        val actual = _uiState.value
        if (actual.cargando || !actual.cargado) return
        val usuario = usuarioActual ?: return
        _uiState.update { it.copy(cargando = true, mensaje = null, esError = false) }
        viewModelScope.launch {
            try {
                val dias = withContext(ioDispatcher) {
                    repositorio.guardar(usuario, actual.fecha, actual.bloqueado, serviciosAsignados)
                }
                withContext(ioDispatcher) {
                    val estadoDia = if (actual.bloqueado) "bloqueado" else "disponible"
                    cola.encolar(
                        OperacionPendiente(
                            tipo = TipoOperacionSync.DISPONIBILIDAD,
                            detalle = "Disponibilidad ${usuario.id} · ${actual.fecha} · $estadoDia"
                        )
                    )
                }
                _uiState.update {
                    it.copy(
                        diasBloqueados = dias,
                        cargando = false,
                        mensaje = t.msgAvailabilitySavedLocal
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                val mensaje = when (error) {
                    is IllegalArgumentException -> PreferenciaIdioma.traducir(error.message)
                    else -> t.msgSaveFailed
                }
                _uiState.update { it.copy(cargando = false, mensaje = mensaje, esError = true) }
            }
        }
    }
}
