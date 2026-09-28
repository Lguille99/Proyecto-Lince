package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.*
import java.time.LocalDate


object DatosPrueba {
    const val CLAVE_DEMO = "Lince123"
    val usuarios = listOf(
        Usuario("DRV-001", "Conductor", "conductor.demo", Rol.CONDUCTOR),
        Usuario("GUI-002", "Guía", "guia.demo", Rol.GUIA),
        Usuario("COO-003", "coordinador", "coordinador.demo", Rol.COORDINADOR)
    )

    fun servicios(hoy: LocalDate): List<Servicio> {
        val manana = hoy.plusDays(1).toString()
        val siguiente = hoy.plusDays(2).toString()
        return listOf(
            Servicio("LIN-001", "transfer de mañana", "Transfer", manana, "08:30",
                "Punto A de prueba", "Terminal de prueba", "DRV-001"),
            Servicio("LIN-002", "Excursión al mirador", "Excursión", manana, "14:00",
                "Punto B de prueba", "Mirador de prueba", "DRV-001"),
            Servicio("LIN-003", "Transfer de regreso", "Transfer", siguiente, "10:00",
                "Terminal de prueba", "Punto A de prueba", "DRV-001"),
            Servicio("LIN-004", "Recorrido guiado", "Servicio guiado", manana, "09:00",
                "Plaza de prueba", "Museo de prueba", "GUI-002"),
            Servicio("LIN-005", "Visita cultural", "Servicio guiado", siguiente, "11:00",
                "Museo de prueba", "Centro de prueba", "GUI-002")
        )
    }
}
