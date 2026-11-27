package cl.apiavengers.lince.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.apiavengers.lince.BuildConfig
import cl.apiavengers.lince.datos.southbound.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SeccionSouthbound { SERVICIOS, GUIAS, TRANSPORTISTAS, CLIENTES, RESUMEN }

@Immutable
data class FiltrosServicios(
    val pagina: Int = 1,
    val busqueda: String = "",
    val estadoServicio: String = "",
    val statusPL: String = "",
    val tipo: String = "",
    val oficina: String = "",
    val fechaDesde: String = "",
    val fechaHasta: String = ""
)

@Immutable
data class FiltrosGuias(
    val pagina: Int = 1,
    val busqueda: String = "",
    val oficina: String = "",
    val idioma: String = ""
)

@Immutable
data class FiltrosTransportistas(
    val pagina: Int = 1,
    val busqueda: String = "",
    val oficina: String = ""
)

@Immutable
data class FiltrosClientes(val pagina: Int = 1, val busqueda: String = "")

@Immutable
data class FiltrosResumen(
    val fechaDesde: String = "",
    val fechaHasta: String = "",
    val oficina: String = ""
)

@Immutable
data class SouthboundUiState(
    val seccion: SeccionSouthbound = SeccionSouthbound.SERVICIOS,
    val iniciado: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val mensajeError: String? = null,
    val requestId: String? = null,
    val esCopia: Boolean = false,
    val guardadoEn: Long = 0,
    val catalogo: CatalogoSouthbound? = null,
    val servicios: PaginaSouthbound<ServicioSouthbound>? = null,
    val filtrosServicios: FiltrosServicios = FiltrosServicios(),
    val guias: PaginaSouthbound<GuiaSouthbound>? = null,
    val filtrosGuias: FiltrosGuias = FiltrosGuias(),
    val transportistas: PaginaSouthbound<TransportistaSouthbound>? = null,
    val filtrosTransportistas: FiltrosTransportistas = FiltrosTransportistas(),
    val clientes: PaginaSouthbound<ClienteApiSouthbound>? = null,
    val filtrosClientes: FiltrosClientes = FiltrosClientes(),
    val resumen: ResumenSouthbound? = null,
    val filtrosResumen: FiltrosResumen = FiltrosResumen(),
    val detalleServicio: ServicioSouthbound? = null,
    val detalleGuia: GuiaSouthbound? = null,
    val detalleTransportista: TransportistaSouthbound? = null,
    val detalleCliente: ClienteApiSouthbound? = null
)

class SouthboundViewModel(
    application: Application,
    private val cliente: ClienteSouthbound = ClienteSouthbound(BuildConfig.SOUTHBOUND_API_KEY, CacheSouthbound.obtener(application).consultas())
) : AndroidViewModel(application) {
    constructor(application: Application) : this(
        application,
        ClienteSouthbound(BuildConfig.SOUTHBOUND_API_KEY, CacheSouthbound.obtener(application).consultas())
    )

    private val _estado = MutableStateFlow(SouthboundUiState())
    val estado = _estado.asStateFlow()
    private var trabajo: Job? = null
    private var solicitud = 0

    fun iniciar() {
        if (_estado.value.iniciado) return
        _estado.update { it.copy(iniciado = true) }
        val filtros = _estado.value.filtrosServicios
        ejecutar {
            val catalogo = cliente.catalogos()
            _estado.update {
                it.copy(catalogo = catalogo.datos, esCopia = catalogo.esCopia, guardadoEn = catalogo.guardadoEn)
            }
            val resultado = cliente.servicios(
                pagina = filtros.pagina,
                busqueda = filtros.busqueda,
                estadoServicio = filtros.estadoServicio,
                statusPL = filtros.statusPL,
                tipo = filtros.tipo,
                oficina = filtros.oficina,
                fechaDesde = filtros.fechaDesde,
                fechaHasta = filtros.fechaHasta
            )
            _estado.update {
                it.copy(servicios = resultado.datos, esCopia = resultado.esCopia, guardadoEn = resultado.guardadoEn)
            }
        }
    }

    fun seleccionar(seccion: SeccionSouthbound) {
        _estado.update { it.copy(seccion = seccion) }
        val actual = _estado.value
        when (seccion) {
            SeccionSouthbound.SERVICIOS -> if (actual.servicios == null) {
                cargarServiciosEnBloque(actual.filtrosServicios)
            }
            SeccionSouthbound.GUIAS -> if (actual.guias == null) {
                cargarGuiasEnBloque(actual.filtrosGuias)
            }
            SeccionSouthbound.TRANSPORTISTAS -> if (actual.transportistas == null) {
                cargarTransportistasEnBloque(actual.filtrosTransportistas)
            }
            SeccionSouthbound.CLIENTES -> if (actual.clientes == null) {
                cargarClientesEnBloque(actual.filtrosClientes)
            }
            SeccionSouthbound.RESUMEN -> if (actual.resumen == null) {
                cargarResumenEnBloque(actual.filtrosResumen)
            }
        }
    }

    fun cargarServicios(
        pagina: Int = _estado.value.filtrosServicios.pagina,
        busqueda: String = _estado.value.filtrosServicios.busqueda,
        estadoServicio: String = _estado.value.filtrosServicios.estadoServicio,
        statusPL: String = _estado.value.filtrosServicios.statusPL,
        tipo: String = _estado.value.filtrosServicios.tipo,
        oficina: String = _estado.value.filtrosServicios.oficina,
        fechaDesde: String = _estado.value.filtrosServicios.fechaDesde,
        fechaHasta: String = _estado.value.filtrosServicios.fechaHasta
    ) {
        val filtros = FiltrosServicios(
            pagina, busqueda, estadoServicio, statusPL, tipo, oficina, fechaDesde.trim(), fechaHasta.trim()
        )
        _estado.update { it.copy(filtrosServicios = filtros, detalleServicio = null) }
        cargarServiciosEnBloque(filtros)
    }

    fun detalleServicio(id: Int) {
        ejecutar {
            val resultado = cliente.detalle(id)
            _estado.update {
                it.copy(
                    detalleServicio = resultado.datos,
                    esCopia = resultado.esCopia,
                    guardadoEn = resultado.guardadoEn
                )
            }
        }
    }

    fun cargarGuias(
        pagina: Int = _estado.value.filtrosGuias.pagina,
        busqueda: String = _estado.value.filtrosGuias.busqueda,
        oficina: String = _estado.value.filtrosGuias.oficina,
        idioma: String = _estado.value.filtrosGuias.idioma
    ) {
        val filtros = FiltrosGuias(pagina, busqueda, oficina, idioma)
        _estado.update { it.copy(filtrosGuias = filtros, detalleGuia = null) }
        cargarGuiasEnBloque(filtros)
    }

    fun detalleGuia(id: Int) {
        ejecutar {
            val resultado = cliente.guia(id)
            _estado.update {
                it.copy(detalleGuia = resultado.datos, esCopia = resultado.esCopia, guardadoEn = resultado.guardadoEn)
            }
        }
    }

    fun cargarTransportistas(
        pagina: Int = _estado.value.filtrosTransportistas.pagina,
        busqueda: String = _estado.value.filtrosTransportistas.busqueda,
        oficina: String = _estado.value.filtrosTransportistas.oficina
    ) {
        val filtros = FiltrosTransportistas(pagina, busqueda, oficina)
        _estado.update { it.copy(filtrosTransportistas = filtros, detalleTransportista = null) }
        cargarTransportistasEnBloque(filtros)
    }

    fun detalleTransportista(id: Int) {
        ejecutar {
            val resultado = cliente.transportista(id)
            _estado.update {
                it.copy(
                    detalleTransportista = resultado.datos,
                    esCopia = resultado.esCopia,
                    guardadoEn = resultado.guardadoEn
                )
            }
        }
    }

    fun cargarClientes(
        pagina: Int = _estado.value.filtrosClientes.pagina,
        busqueda: String = _estado.value.filtrosClientes.busqueda
    ) {
        val filtros = FiltrosClientes(pagina, busqueda)
        _estado.update { it.copy(filtrosClientes = filtros, detalleCliente = null) }
        cargarClientesEnBloque(filtros)
    }

    fun detalleCliente(id: Int) {
        ejecutar {
            val resultado = cliente.cliente(id)
            _estado.update {
                it.copy(detalleCliente = resultado.datos, esCopia = resultado.esCopia, guardadoEn = resultado.guardadoEn)
            }
        }
    }

    fun cargarResumen(
        fechaDesde: String = _estado.value.filtrosResumen.fechaDesde,
        fechaHasta: String = _estado.value.filtrosResumen.fechaHasta,
        oficina: String = _estado.value.filtrosResumen.oficina
    ) {
        val filtros = FiltrosResumen(fechaDesde.trim(), fechaHasta.trim(), oficina)
        _estado.update { it.copy(filtrosResumen = filtros) }
        cargarResumenEnBloque(filtros)
    }

    fun cerrarDetalle() {
        _estado.update {
            it.copy(
                detalleServicio = null,
                detalleGuia = null,
                detalleTransportista = null,
                detalleCliente = null
            )
        }
    }

    private fun cargarServiciosEnBloque(filtros: FiltrosServicios) {
        ejecutar {
            val resultado = cliente.servicios(
                pagina = filtros.pagina,
                busqueda = filtros.busqueda,
                estadoServicio = filtros.estadoServicio,
                statusPL = filtros.statusPL,
                tipo = filtros.tipo,
                oficina = filtros.oficina,
                fechaDesde = filtros.fechaDesde,
                fechaHasta = filtros.fechaHasta
            )
            _estado.update {
                it.copy(servicios = resultado.datos, esCopia = resultado.esCopia, guardadoEn = resultado.guardadoEn)
            }
        }
    }

    private fun cargarGuiasEnBloque(filtros: FiltrosGuias) {
        ejecutar {
            val resultado = cliente.guias(
                pagina = filtros.pagina,
                busqueda = filtros.busqueda,
                oficina = filtros.oficina,
                idioma = filtros.idioma
            )
            _estado.update {
                it.copy(guias = resultado.datos, esCopia = resultado.esCopia, guardadoEn = resultado.guardadoEn)
            }
        }
    }

    private fun cargarTransportistasEnBloque(filtros: FiltrosTransportistas) {
        ejecutar {
            val resultado = cliente.transportistas(
                pagina = filtros.pagina,
                busqueda = filtros.busqueda,
                oficina = filtros.oficina
            )
            _estado.update {
                it.copy(
                    transportistas = resultado.datos,
                    esCopia = resultado.esCopia,
                    guardadoEn = resultado.guardadoEn
                )
            }
        }
    }

    private fun cargarClientesEnBloque(filtros: FiltrosClientes) {
        ejecutar {
            val resultado = cliente.clientes(pagina = filtros.pagina, busqueda = filtros.busqueda)
            _estado.update {
                it.copy(clientes = resultado.datos, esCopia = resultado.esCopia, guardadoEn = resultado.guardadoEn)
            }
        }
    }

    private fun cargarResumenEnBloque(filtros: FiltrosResumen) {
        ejecutar {
            val resultado = cliente.resumen(
                fechaDesde = filtros.fechaDesde,
                fechaHasta = filtros.fechaHasta,
                oficina = filtros.oficina
            )
            _estado.update {
                it.copy(resumen = resultado.datos, esCopia = resultado.esCopia, guardadoEn = resultado.guardadoEn)
            }
        }
    }

    private fun ejecutar(bloque: suspend () -> Unit) {
        trabajo?.cancel()
        val actual = ++solicitud
        _estado.update {
            it.copy(cargando = true, error = null, mensajeError = null, requestId = null, esCopia = false)
        }
        trabajo = viewModelScope.launch {
            try {
                bloque()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: ErrorSouthbound) {
                if (actual == solicitud) {
                    _estado.update {
                        it.copy(
                            error = error.codigo,
                            mensajeError = error.mensajeServidor,
                            requestId = error.requestId
                        )
                    }
                }
            } catch (error: Exception) {
                if (actual == solicitud) {
                    _estado.update { it.copy(error = "datos", mensajeError = error.message) }
                }
            } finally {
                if (actual == solicitud) _estado.update { it.copy(cargando = false) }
            }
        }
    }


}
