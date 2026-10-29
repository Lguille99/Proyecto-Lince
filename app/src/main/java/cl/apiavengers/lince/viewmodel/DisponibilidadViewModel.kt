package cl.apiavengers.lince.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.apiavengers.lince.datos.local.AlmacenDisponibilidadRoom
import cl.apiavengers.lince.datos.local.LinceDatabase
import cl.apiavengers.lince.datos.ArchivoDisponibilidad
import cl.apiavengers.lince.datos.ErrorSupabase
import cl.apiavengers.lince.datos.RepositorioDisponibilidad
import cl.apiavengers.lince.datos.SesionSupabase
import cl.apiavengers.lince.modelo.DisponibilidadUiState
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DisponibilidadViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = RepositorioDisponibilidad(
        AlmacenDisponibilidadRoom(LinceDatabase.obtener(application).disponibilidadDao(), ArchivoDisponibilidad(application))
    )
    private val _uiState = MutableStateFlow(DisponibilidadUiState())
    val uiState = _uiState.asStateFlow()
    private var usuarioActual: Usuario? = null

    fun cargar(usuario: Usuario) {
        usuarioActual = usuario
        _uiState.value = DisponibilidadUiState()
        viewModelScope.launch {
            try {
                val cliente = SesionSupabase.cliente?.takeIf { it.activo }
                val dias = if (cliente != null) {
                    cliente.cargarDisponibilidad()
                } else {
                    withContext(Dispatchers.IO) { repositorio.consultar(usuario) }
                }
                _uiState.update { it.copy(diasBloqueados = dias, bloqueado = it.fecha in dias, cargando = false, cargado = true) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _uiState.update { it.copy(cargando = false, esError = true, mensaje = "No se pudo leer tu agenda. Intenta nuevamente.") }
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
                val cliente = SesionSupabase.cliente?.takeIf { it.activo }
                if (cliente != null) {
                    cliente.guardarDisponibilidad(actual.fecha, actual.bloqueado)
                    val dias = if (actual.bloqueado) {
                        actual.diasBloqueados + actual.fecha
                    } else {
                        actual.diasBloqueados - actual.fecha
                    }
                    _uiState.update {
                        it.copy(
                            diasBloqueados = dias,
                            cargando = false,
                            mensaje = "Disponibilidad guardada en Supabase."
                        )
                    }
                } else {
                    val dias = withContext(Dispatchers.IO) {
                        repositorio.guardar(usuario, actual.fecha, actual.bloqueado, serviciosAsignados)
                    }
                    _uiState.update {
                        it.copy(
                            diasBloqueados = dias,
                            cargando = false,
                            mensaje = "Disponibilidad guardada en este dispositivo."
                        )
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                val mensaje = when {
                    error is IllegalArgumentException -> error.message
                    error is ErrorSupabase -> error.message
                    else -> "No se pudo guardar. Intenta nuevamente."
                }
                _uiState.update { it.copy(cargando = false, mensaje = mensaje, esError = true) }
            }
        }
    }
}
