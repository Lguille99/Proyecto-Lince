package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.*
import java.time.LocalDate

object DatosPrueba {
    const val CLAVE_DEMO = "Lince123"

    val usuarios = listOf(
        Usuario(
            "DRV-001", "Martín Conductor", "conductor.demo", "conductor@gmail.com", Rol.CONDUCTOR,
            "+56 9 1234 5678", Disponibilidad.DISPONIBLE, "Santiago Centro",
            listOf(Idioma.ESPANOL, Idioma.INGLES)
        ),
        Usuario(
            "GUI-002", "Valentina Guía", "guia.demo", "guia@gmail.com", Rol.GUIA,
            "+56 9 8765 4321", Disponibilidad.EN_SERVICIO, "Providencia",
            listOf(Idioma.ESPANOL, Idioma.INGLES)
        ),
        Usuario(
            "COO-003", "Camila Coordinadora", "coordinador.demo", "coordinador@gmail.com", Rol.COORDINADOR,
            "+56 9 1111 2222", Disponibilidad.DISPONIBLE, "Oficina central",
            listOf(Idioma.ESPANOL, Idioma.INGLES)
        ),
        Usuario(
            "DRV-002", "Diego Ruta", "conductor2.demo", "diego.ruta@gmail.com", Rol.CONDUCTOR,
            "+56 9 3333 4444", Disponibilidad.NO_DISPONIBLE, "Maipú",
            listOf(Idioma.ESPANOL)
        ),
        Usuario(
            "GUI-003", "Sofía Senderos", "guia2.demo", "sofia.senderos@gmail.com", Rol.GUIA,
            "+56 9 5555 6666", Disponibilidad.DISPONIBLE, "Viña del Mar",
            listOf(Idioma.ESPANOL, Idioma.INGLES)
        ),
        Usuario(
            "GUI-004", "Pedro Mirador", "guia3.demo", "pedro.mirador@gmail.com", Rol.GUIA,
            "+56 9 7777 8888", Disponibilidad.DISPONIBLE, "Valparaíso",
            listOf(Idioma.ESPANOL)
        )
    )

    private fun sello(fecha: LocalDate, hora: String) = "${fecha}T$hora"

    fun servicios(hoy: LocalDate): List<Servicio> {
        val manana = hoy.plusDays(1).toString()
        val siguiente = hoy.plusDays(2).toString()
        return listOf(
            Servicio(
                "LIN-001", "Transfer de mañana", "Transfer", manana, "08:30",
                "Hotel Plaza", "Terminal de prueba", "DRV-001", EstadoServicio.PENDIENTE,
                idiomaRequerido = Idioma.ESPANOL, modalidad = Modalidad.TRANSFER,
                hotelRecogida = "Hotel Plaza", tarifa = 25_000, pasajeros = 2,
                resenaHistorica = "Traslado directo desde el hotel al terminal, sin paradas intermedias."
            ),
            Servicio(
                "LIN-002", "Excursión al mirador", "Excursión", manana, "14:00",
                "Hotel Plaza", "Mirador de prueba", "DRV-001", EstadoServicio.CONFIRMADA,
                idiomaRequerido = Idioma.INGLES, modalidad = Modalidad.MEDIO_DIA,
                almuerzoIncluido = true, hotelRecogida = "Hotel Plaza", tarifa = 45_000,
                pasajeros = 4, itinerario = listOf("Hotel Plaza", "Mirador de prueba", "Restaurante de prueba"),
                resenaHistorica = "El mirador fue habilitado en 1998 y conserva senderos de la ruta original."
            ),
            Servicio(
                "LIN-003", "Transfer de regreso", "Transfer", siguiente, "10:00",
                "Terminal de prueba", "Hotel Plaza", "DRV-001", EstadoServicio.PENDIENTE,
                idiomaRequerido = Idioma.ESPANOL, modalidad = Modalidad.TRANSFER,
                hotelRecogida = "Hotel Plaza", tarifa = 25_000, pasajeros = 2,
                resenaHistorica = "Retorno al hotel con equipaje; se recomienda confirmar el número de maletas."
            ),
            Servicio(
                "LIN-004", "Recorrido guiado", "Servicio guiado", manana, "09:00",
                "Plaza de prueba", "Museo de prueba", "GUI-002", EstadoServicio.CONFIRMADA,
                idiomaRequerido = Idioma.ESPANOL, modalidad = Modalidad.MEDIO_DIA,
                hotelRecogida = "Plaza de prueba", tarifa = 40_000, pasajeros = 3,
                itinerario = listOf("Plaza de prueba", "Museo de prueba"),
                resenaHistorica = "La plaza fue el centro cívico de la ciudad durante el siglo XIX."
            ),
            Servicio(
                "LIN-005", "Visita cultural", "Servicio guiado", siguiente, "11:00",
                "Museo de prueba", "Centro de prueba", "GUI-002", EstadoServicio.PENDIENTE,
                idiomaRequerido = Idioma.INGLES, modalidad = Modalidad.MEDIO_DIA,
                hotelRecogida = "Museo de prueba", tarifa = 42_000, pasajeros = 5,
                itinerario = listOf("Museo de prueba", "Centro de prueba"),
                resenaHistorica = "El centro histórico conserva fachadas restauradas y murales del puerto."
            ),
            Servicio(
                "LIN-006", "Transfer aeropuerto", "Transfer", manana, "07:00",
                "Hotel de prueba", "Aeropuerto de prueba", "DRV-002", EstadoServicio.PENDIENTE,
                idiomaRequerido = Idioma.INGLES, modalidad = Modalidad.TRANSFER,
                hotelRecogida = "Hotel de prueba", tarifa = 30_000, pasajeros = 2,
                resenaHistorica = "Salida temprana; considerar margen adicional por tráfico de la mañana."
            ),
            Servicio(
                "LIN-007", "Tour viña y mar", "Excursión", siguiente, "10:30",
                "Hotel Marina", "Viña de prueba", "GUI-003", EstadoServicio.CONFIRMADA,
                idiomaRequerido = Idioma.ESPANOL, modalidad = Modalidad.DIA_COMPLETO,
                almuerzoIncluido = true, hotelRecogida = "Hotel Marina", tarifa = 65_000,
                pasajeros = 8, itinerario = listOf("Hotel Marina", "Viña de prueba", "Costa de prueba"),
                resenaHistorica = "La viña familiar produce vinos costeros y mantiene tours desde 1952."
            ),
            Servicio(
                "LIN-008", "City tour Valparaíso", "Servicio guiado", manana, "15:00",
                "Plaza de prueba", "Cerro de prueba", "GUI-004", EstadoServicio.PENDIENTE,
                idiomaRequerido = Idioma.ESPANOL, modalidad = Modalidad.MEDIO_DIA,
                hotelRecogida = "Plaza de prueba", tarifa = 38_000, pasajeros = 6,
                itinerario = listOf("Plaza de prueba", "Cerro de prueba", "Mirador de prueba"),
                resenaHistorica = "El cerro alberga ascensores patrimoniales declarados monumento nacional."
            ),
            Servicio(
                "LIN-009", "City tour día completo", "City tour", hoy.toString(), "08:00",
                "Hotel Plaza", "Cerro de prueba", "GUI-003", EstadoServicio.CONFIRMADA,
                acompananteId = "DRV-001", idiomaRequerido = Idioma.INGLES,
                modalidad = Modalidad.DIA_COMPLETO, almuerzoIncluido = true,
                hotelRecogida = "Hotel Plaza", tarifa = 65_000, pasajeros = 10,
                itinerario = listOf("Hotel Plaza", "Centro histórico", "Cerro de prueba", "Almuerzo", "Mirador"),
                resenaHistorica = "Recorrido de día completo por el casco histórico y sus miradores."
            ),
            Servicio(
                "LIN-010", "Traslado aeropuerto", "Transfer", hoy.toString(), "18:00",
                "Hotel de prueba", "Aeropuerto de prueba", "DRV-002", EstadoServicio.PENDIENTE,
                idiomaRequerido = Idioma.INGLES, modalidad = Modalidad.TRANSFER,
                hotelRecogida = "Hotel de prueba", tarifa = 30_000, pasajeros = 1,
                resenaHistorica = "Traslado nocturno; confirmar vuelo y terminal antes de salir."
            )
        )
    }

    fun historial(hoy: LocalDate): List<Servicio> {
        return listOf(
            Servicio(
                "LIN-101", "Transfer centro histórico", "Transfer", hoy.minusDays(1).toString(), "09:00",
                "Hotel de prueba", "Centro de prueba", "DRV-001", EstadoServicio.COMPLETADA,
                idiomaRequerido = Idioma.ESPANOL, modalidad = Modalidad.TRANSFER,
                hotelRecogida = "Hotel de prueba", tarifa = 25_000, pasajeros = 2,
                respondidoPor = "DRV-001", respondidoEn = sello(hoy.minusDays(2), "16:00"),
                inicioEn = sello(hoy.minusDays(1), "08:45"), terminoEn = sello(hoy.minusDays(1), "09:40"),
                inicioPor = "DRV-001", ubicacionInicio = "Hotel de prueba", puntaje = 5,
                calificadoPor = "COO-003", calificadoEn = sello(hoy.minusDays(1), "12:00")
            ),
            Servicio(
                "LIN-102", "Tour monumento", "Servicio guiado", hoy.minusDays(3).toString(), "11:00",
                "Plaza de prueba", "Monumento de prueba", "GUI-002", EstadoServicio.COMPLETADA,
                idiomaRequerido = Idioma.INGLES, modalidad = Modalidad.MEDIO_DIA,
                hotelRecogida = "Plaza de prueba", tarifa = 40_000, pasajeros = 4,
                respondidoPor = "GUI-002", respondidoEn = sello(hoy.minusDays(4), "10:00"),
                inicioEn = sello(hoy.minusDays(3), "10:50"), terminoEn = sello(hoy.minusDays(3), "13:10"),
                inicioPor = "GUI-002", ubicacionInicio = "Plaza de prueba", puntaje = 4,
                calificadoPor = "COO-003", calificadoEn = sello(hoy.minusDays(3), "18:00")
            ),
            Servicio(
                "LIN-103", "Transfer hotel", "Transfer", hoy.minusDays(2).toString(), "18:00",
                "Terminal de prueba", "Hotel de prueba", "DRV-002", EstadoServicio.RECHAZADA,
                idiomaRequerido = Idioma.ESPANOL, modalidad = Modalidad.TRANSFER,
                hotelRecogida = "Hotel de prueba", tarifa = 30_000, pasajeros = 2,
                respondidoPor = "DRV-002", respondidoEn = sello(hoy.minusDays(3), "19:20"),
                motivoRechazo = "Choque de agenda con otro servicio."
            ),
            Servicio(
                "LIN-104", "Excursión costera", "Excursión", hoy.minusDays(4).toString(), "08:30",
                "Centro de prueba", "Playa de prueba", "GUI-003", EstadoServicio.COMPLETADA,
                idiomaRequerido = Idioma.ESPANOL, modalidad = Modalidad.DIA_COMPLETO,
                almuerzoIncluido = true, hotelRecogida = "Centro de prueba", tarifa = 65_000, pasajeros = 9,
                respondidoPor = "GUI-003", respondidoEn = sello(hoy.minusDays(5), "12:00"),
                inicioEn = sello(hoy.minusDays(4), "08:20"), terminoEn = sello(hoy.minusDays(4), "17:30"),
                inicioPor = "GUI-003", ubicacionInicio = "Centro de prueba", puntaje = 5,
                calificadoPor = "COO-003", calificadoEn = sello(hoy.minusDays(4), "20:00")
            ),
            Servicio(
                "LIN-105", "Visita museo", "Servicio guiado", hoy.minusDays(5).toString(), "10:00",
                "Plaza de prueba", "Museo de prueba", "GUI-004", EstadoServicio.COMPLETADA,
                idiomaRequerido = Idioma.ESPANOL, modalidad = Modalidad.MEDIO_DIA,
                hotelRecogida = "Plaza de prueba", tarifa = 38_000, pasajeros = 5,
                respondidoPor = "GUI-004", respondidoEn = sello(hoy.minusDays(6), "09:00"),
                inicioEn = sello(hoy.minusDays(5), "09:50"), terminoEn = sello(hoy.minusDays(5), "12:40"),
                inicioPor = "GUI-004", ubicacionInicio = "Plaza de prueba", puntaje = 3,
                calificadoPor = "COO-003", calificadoEn = sello(hoy.minusDays(5), "19:00")
            ),
            Servicio(
                "LIN-106", "Transfer noche", "Transfer", hoy.minusDays(6).toString(), "22:00",
                "Aeropuerto de prueba", "Hotel de prueba", "DRV-001", EstadoServicio.COMPLETADA,
                idiomaRequerido = Idioma.INGLES, modalidad = Modalidad.TRANSFER,
                hotelRecogida = "Aeropuerto de prueba", tarifa = 25_000, pasajeros = 3,
                respondidoPor = "DRV-001", respondidoEn = sello(hoy.minusDays(7), "15:00"),
                inicioEn = sello(hoy.minusDays(6), "21:45"), terminoEn = sello(hoy.minusDays(6), "22:35"),
                inicioPor = "DRV-001", ubicacionInicio = "Aeropuerto de prueba", puntaje = 4,
                calificadoPor = "COO-003", calificadoEn = sello(hoy.minusDays(6), "23:30")
            )
        )
    }
}
