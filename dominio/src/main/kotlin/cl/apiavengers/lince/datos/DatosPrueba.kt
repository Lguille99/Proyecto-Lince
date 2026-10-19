package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.*
import java.time.LocalDate


object DatosPrueba {
    const val CLAVE_DEMO = "Lince123"
    val usuarios = listOf(
        Usuario(
            "DRV-001", "Martín Conductor", "conductor.demo", "conductor@gmail.com", Rol.CONDUCTOR,
            "+56 9 1234 5678", Disponibilidad.DISPONIBLE, "Santiago Centro"
        ),
        Usuario(
            "GUI-002", "Valentina Guía", "guia.demo", "guia@gmail.com", Rol.GUIA,
            "+56 9 8765 4321", Disponibilidad.EN_SERVICIO, "Providencia"
        ),
        Usuario(
            "COO-003", "Camila Coordinadora", "coordinador.demo", "coordinador@gmail.com", Rol.COORDINADOR,
            "+56 9 1111 2222", Disponibilidad.DISPONIBLE, "Oficina central"
        ),
        Usuario(
            "DRV-002", "Diego Ruta", "conductor2.demo", "diego.ruta@gmail.com", Rol.CONDUCTOR,
            "+56 9 3333 4444", Disponibilidad.NO_DISPONIBLE, "Maipú"
        ),
        Usuario(
            "GUI-003", "Sofía Senderos", "guia2.demo", "sofia.senderos@gmail.com", Rol.GUIA,
            "+56 9 5555 6666", Disponibilidad.DISPONIBLE, "Viña del Mar"
        ),
        Usuario(
            "GUI-004", "Pedro Mirador", "guia3.demo", "pedro.mirador@gmail.com", Rol.GUIA,
            "+56 9 7777 8888", Disponibilidad.DISPONIBLE, "Valparaíso"
        )
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
