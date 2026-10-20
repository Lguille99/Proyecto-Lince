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
                "Punto A de prueba", "Terminal de prueba", "DRV-001", EstadoServicio.PENDIENTE),
            Servicio("LIN-002", "Excursión al mirador", "Excursión", manana, "14:00",
                "Punto B de prueba", "Mirador de prueba", "DRV-001", EstadoServicio.CONFIRMADA),
            Servicio("LIN-003", "Transfer de regreso", "Transfer", siguiente, "10:00",
                "Terminal de prueba", "Punto A de prueba", "DRV-001", EstadoServicio.PENDIENTE),
            Servicio("LIN-004", "Recorrido guiado", "Servicio guiado", manana, "09:00",
                "Plaza de prueba", "Museo de prueba", "GUI-002", EstadoServicio.CONFIRMADA),
            Servicio("LIN-005", "Visita cultural", "Servicio guiado", siguiente, "11:00",
                "Museo de prueba", "Centro de prueba", "GUI-002", EstadoServicio.PENDIENTE),
            Servicio("LIN-006", "Transfer aeropuerto", "Transfer", manana, "07:00",
                "Hotel de prueba", "Aeropuerto de prueba", "DRV-002", EstadoServicio.PENDIENTE),
            Servicio("LIN-007", "Tour viña y mar", "Excursión", siguiente, "10:30",
                "Centro de prueba", "Viña de prueba", "GUI-003", EstadoServicio.CONFIRMADA),
            Servicio("LIN-008", "City tour Valparaíso", "Servicio guiado", manana, "15:00",
                "Plaza de prueba", "Cerro de prueba", "GUI-004", EstadoServicio.PENDIENTE)
        )
    }

    fun historial(hoy: LocalDate): List<Servicio> {
        return listOf(
            Servicio("LIN-101", "Transfer centro histórico", "Transfer", hoy.minusDays(1).toString(), "09:00",
                "Hotel de prueba", "Centro de prueba", "DRV-001", EstadoServicio.COMPLETADA),
            Servicio("LIN-102", "Tour monumento", "Servicio guiado", hoy.minusDays(3).toString(), "11:00",
                "Plaza de prueba", "Monumento de prueba", "GUI-002", EstadoServicio.COMPLETADA),
            Servicio("LIN-103", "Transfer hotel", "Transfer", hoy.minusDays(2).toString(), "18:00",
                "Terminal de prueba", "Hotel de prueba", "DRV-002", EstadoServicio.RECHAZADA),
            Servicio("LIN-104", "Excursión costera", "Excursión", hoy.minusDays(4).toString(), "08:30",
                "Centro de prueba", "Playa de prueba", "GUI-003", EstadoServicio.COMPLETADA),
            Servicio("LIN-105", "Visita museo", "Servicio guiado", hoy.minusDays(5).toString(), "10:00",
                "Plaza de prueba", "Museo de prueba", "GUI-004", EstadoServicio.COMPLETADA),
            Servicio("LIN-106", "Transfer noche", "Transfer", hoy.minusDays(6).toString(), "22:00",
                "Aeropuerto de prueba", "Hotel de prueba", "DRV-001", EstadoServicio.COMPLETADA)
        )
    }
}
