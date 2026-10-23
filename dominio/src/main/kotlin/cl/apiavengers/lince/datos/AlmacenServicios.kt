package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.EstadoServicio

interface AlmacenServicios {
    fun preparar(servicios: List<Servicio>)
    fun consultar(): List<Servicio>
    fun responder(id: String, usuarioId: String, estado: EstadoServicio, motivo: String?, fechaHora: String): Boolean
}

class AlmacenServiciosMemoria : AlmacenServicios {
    private val servicios = linkedMapOf<String, Servicio>()
    override fun preparar(servicios: List<Servicio>) {
        servicios.forEach { this.servicios.putIfAbsent(it.id, it) }
    }
    override fun consultar() = servicios.values.toList()
    override fun responder(id: String, usuarioId: String, estado: EstadoServicio, motivo: String?, fechaHora: String): Boolean {
        val servicio = servicios[id] ?: return false
        if (servicio.asignadoA != usuarioId || servicio.estado != EstadoServicio.PENDIENTE || servicio.historico) return false
        servicios[id] = servicio.copy(estado = estado, motivoRechazo = motivo, respondidoEn = fechaHora)
        return true
    }
}
