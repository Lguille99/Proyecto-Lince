package cl.apiavengers.lince.datos.local

import cl.apiavengers.lince.datos.AlmacenDisponibilidad
import java.time.LocalDate

class AlmacenDisponibilidadRoom(
    private val dao: DisponibilidadDao,
    private val anterior: AlmacenDisponibilidad
) : AlmacenDisponibilidad {
    private fun importar(cuenta: String) {
        if (!dao.estaImportada(cuenta)) {
            dao.importar(cuenta, anterior.leer(cuenta).map { it.toString() })
        }
    }

    override fun leer(cuenta: String): Set<LocalDate> {
        importar(cuenta)
        return dao.consultar(cuenta).map { LocalDate.parse(it) }.toSet()
    }

    override fun guardar(cuenta: String, fechas: Set<LocalDate>) {
        importar(cuenta)
        dao.reemplazar(cuenta, fechas.map { it.toString() })
    }
}
