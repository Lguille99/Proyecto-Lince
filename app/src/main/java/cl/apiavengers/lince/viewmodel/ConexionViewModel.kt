package cl.apiavengers.lince.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.apiavengers.lince.datos.MonitorConexion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ConexionViewModel(application: Application) : AndroidViewModel(application) {
    val conectado = MonitorConexion(application).conectado
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
