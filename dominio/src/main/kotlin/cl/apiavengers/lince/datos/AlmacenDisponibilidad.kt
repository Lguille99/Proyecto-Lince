package cl.apiavengers.lince.datos

import java.time.LocalDate

interface AlmacenDisponibilidad {
    fun leer(cuenta: String): Set<LocalDate>
    fun guardar(cuenta: String, fechas: Set<LocalDate>)
}
