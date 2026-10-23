package cl.apiavengers.lince.datos.local

import cl.apiavengers.lince.datos.AlmacenServicios
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.EstadoServicio

class AlmacenServiciosRoom(private val dao: ServicioDao) : AlmacenServicios {
    override fun preparar(servicios: List<Servicio>) = dao.insertarIniciales(servicios.map { ServicioEntity.desde(it) })
    override fun consultar() = dao.consultar().map { it.aModelo() }
    override fun responder(id: String, usuarioId: String, estado: EstadoServicio, motivo: String?, fechaHora: String) =
        dao.responder(id, usuarioId, estado.name, motivo, fechaHora) == 1
}
