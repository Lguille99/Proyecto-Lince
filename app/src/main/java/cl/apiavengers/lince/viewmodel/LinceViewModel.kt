package cl.apiavengers.lince.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario

class LinceViewModel : ViewModel() {
    private val repositorio = RepositorioLince()
    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var servicios by mutableStateOf<List<Servicio>>(emptyList())
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun iniciarSesion(cuenta: String, clave: String) {
        usuario = null
        servicios = emptyList()
        error = null
        try {
            usuario = repositorio.iniciarSesion(cuenta, clave)
            servicios = repositorio.consultarServicios()
        } catch (problema: IllegalArgumentException) {
            error = problema.message
        }
    }

    fun cerrarSesion() {
        repositorio.cerrarSesion()
        usuario = null
        servicios = emptyList()
        error = null
    }
}
