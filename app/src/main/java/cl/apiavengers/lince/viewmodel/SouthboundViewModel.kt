package cl.apiavengers.lince.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.apiavengers.lince.BuildConfig
import cl.apiavengers.lince.datos.southbound.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SouthboundUiState(
    val cargando: Boolean = false,
    val pagina: PaginaSouthbound? = null,
    val detalle: ServicioSouthbound? = null,
    val numeroPagina: Int = 1,
    val busqueda: String = "",
    val filtro: String = "",
    val error: String? = null,
    val esCopia: Boolean = false,
    val guardadoEn: Long = 0
)

class SouthboundViewModel(application: Application) : AndroidViewModel(application) {
    private val cliente = ClienteSouthbound(
        BuildConfig.SOUTHBOUND_API_KEY, CacheSouthbound.obtener(application).consultas()
    )
    private val _estado = MutableStateFlow(SouthboundUiState())
    val estado = _estado.asStateFlow()
    private var trabajo: Job? = null
    private var solicitud = 0

    fun cargar(pagina: Int = 1, busqueda: String = _estado.value.busqueda, filtro: String = _estado.value.filtro) {
        ejecutar {
            _estado.update { it.copy(numeroPagina = pagina, busqueda = busqueda, filtro = filtro, pagina = null, detalle = null) }
            val resultado = cliente.servicios(pagina, busqueda, filtro)
            _estado.update {
                it.copy(pagina = resultado.datos, esCopia = resultado.esCopia, guardadoEn = resultado.guardadoEn)
            }
        }
    }

    fun detalle(id: Int) {
        ejecutar {
            _estado.update { it.copy(detalle = null) }
            val resultado = cliente.detalle(id)
            _estado.update {
                it.copy(detalle = resultado.datos, esCopia = resultado.esCopia, guardadoEn = resultado.guardadoEn)
            }
        }
    }

    private fun ejecutar(bloque: suspend () -> Unit) {
        trabajo?.cancel()
        val actual = ++solicitud
        _estado.update { it.copy(cargando = true, error = null, esCopia = false) }
        trabajo = viewModelScope.launch {
            try {
                bloque()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                if (actual == solicitud) {
                    _estado.update { it.copy(error = (error as? ErrorSouthbound)?.codigo ?: "datos") }
                }
            } finally {
                if (actual == solicitud) _estado.update { it.copy(cargando = false) }
            }
        }
    }
}
