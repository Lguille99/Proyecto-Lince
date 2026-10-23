package cl.apiavengers.lince.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.io.IOException
import cl.apiavengers.lince.datos.AutenticacionSupabase
import androidx.lifecycle.ViewModel
import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.modelo.ResumenCoordinador
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario

class LinceViewModel : ViewModel() {
    private val autenticacion = AutenticacionSupabase()
    var cargando by mutableStateOf(false)
        private set
    private val repositorio = RepositorioLince()
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

    fun iniciarSesion(cuenta: String, clave: String) {
        if (cargando) return
        cerrarSesion()
        error = null
        cargando = true
        viewModelScope.launch {
            try {
                val perfil = autenticacion.iniciarSesion(cuenta, clave)
                repositorio.establecerUsuario(perfil)
                servicios = repositorio.consultarServicios()
                historial = repositorio.consultarHistorial()
                equipo = repositorio.consultarEquipo()
                if (perfil.rol == Rol.COORDINADOR) resumen = repositorio.resumenCoordinador()
                usuario = perfil
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (problema: IOException) {
                error = "No se pudo conectar. Revisa tu conexión a Internet."
            } catch (problema: IllegalArgumentException) {
                error = problema.message
            } catch (problema: Exception) {
                error = "No se pudo cargar tu perfil. Intenta nuevamente."
            } finally {
                cargando = false
            }
        }
    }

    fun esCoordinador(): Boolean = usuario?.rol == Rol.COORDINADOR

    // Función para actualizar los datos del usuario en memoria durante la sesión
    fun actualizarUsuario(nuevoNombre: String, nuevoCorreo: String) {
        usuario = usuario?.copy(
            nombre = nuevoNombre,
            correo = nuevoCorreo
        )
    }

    fun sincronizar() {
        if (usuario?.rol == Rol.COORDINADOR) {
            resumen = repositorio.resumenCoordinador()
            servicios = repositorio.consultarServicios()
            historial = repositorio.consultarHistorial()
            equipo = repositorio.consultarEquipo()
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
    }

    fun obtenerPersona(id: String): Usuario? {
        return try {
            repositorio.obtenerUsuarioPorId(id)
        } catch (_: Exception) {
            null
        }
    }

    fun obtenerServicio(id: String): Servicio? {
        return try {
            repositorio.obtenerServicioPorId(id)
        } catch (_: Exception) {
            null
        }
    }

    fun serviciosDe(usuarioId: String): List<Servicio> {
        return try {
            repositorio.consultarServiciosDe(usuarioId)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
