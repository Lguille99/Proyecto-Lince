package cl.apiavengers.lince.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.apiavengers.lince.datos.MonitorConexion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ConexionViewModel(
    private val monitor: MonitorConexion
) : ViewModel() {
    val conectado: StateFlow<Boolean> = monitor.conectado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    constructor(application: android.app.Application) : this(MonitorConexion(application))
}
