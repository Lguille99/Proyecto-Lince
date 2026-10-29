package cl.apiavengers.lince.datos.local

import cl.apiavengers.lince.datos.AlmacenEstado
import cl.apiavengers.lince.datos.EstadoGuardado

class AlmacenEstadoRoom(
    private val dao: EstadoDao,
    private val anterior: AlmacenEstado? = null
) : AlmacenEstado {
    override fun leer(): EstadoGuardado {
        val servicios = dao.consultarServicios().associate { it.id to it.aModelo() }
        val perfiles = dao.consultarPerfiles().associate { it.id to it.aModelo() }
        if (servicios.isNotEmpty() || perfiles.isNotEmpty()) {
            return EstadoGuardado(servicios, perfiles)
        }
        val legado = anterior?.leer() ?: return EstadoGuardado()
        if (legado.servicios.isNotEmpty() || legado.perfiles.isNotEmpty()) {
            guardar(legado)
        }
        return legado
    }

    override fun guardar(estado: EstadoGuardado) {
        dao.reemplazar(
            servicios = estado.servicios.map { (id, cambio) ->
                EstadoServicioEntity.desde(id, cambio)
            },
            perfiles = estado.perfiles.map { (id, cambio) ->
                PerfilEntity.desde(id, cambio)
            }
        )
    }
}
