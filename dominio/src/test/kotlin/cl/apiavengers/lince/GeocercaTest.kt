package cl.apiavengers.lince

import cl.apiavengers.lince.datos.AlmacenEstadoMemoria
import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Ubicacion
import cl.apiavengers.lince.modelo.distanciaMetros
import cl.apiavengers.lince.modelo.evaluarLlegada
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GeocercaTest {
    private val reloj = Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneOffset.UTC)

    private fun repo() = RepositorioLince(AlmacenEstadoMemoria(), reloj)

    private val dentro = Ubicacion(-33.4425, -70.6540, precisionMetros = 8.0)
    private val fuera = Ubicacion(-33.4000, -70.6000, precisionMetros = 12.0)

    @Test fun distanciaCeroEnElMismoPunto() {
        assertEquals(0.0, distanciaMetros(-33.4425, -70.6540, -33.4425, -70.6540), 0.01)
    }

    @Test fun evaluaDentroFueraYTardanza() {
        val servicio = Servicio(
            id = "LIN-X",
            titulo = "Prueba",
            tipo = "Transfer",
            fecha = "2026-10-19",
            hora = "12:10",
            recogida = "Hotel",
            destino = "Destino",
            asignadoA = "DRV-001",
            hotelRecogida = "Hotel",
            latitudRecogida = -33.4425,
            longitudRecogida = -70.6540,
            radioGeocercaMetros = 150
        )
        val ahora = java.time.LocalDateTime.of(2026, 10, 19, 12, 0)
        val llegada = evaluarLlegada(servicio, dentro, ahora)
        assertNotNull(llegada)
        assertTrue(llegada.dentro)
        assertEquals(10, llegada.minutosAntes)
        assertTrue(!llegada.tardanza)

        val lejana = evaluarLlegada(servicio, fuera, ahora)
        assertNotNull(lejana)
        assertTrue(lejana.fueraDeZona)

        val tarde = evaluarLlegada(servicio, dentro, java.time.LocalDateTime.of(2026, 10, 19, 12, 20))
        assertNotNull(tarde)
        assertTrue(tarde.tardanza)
    }

    @Test fun sinCoordenadasNoEvaluaGeocerca() {
        val servicio = Servicio(
            id = "LIN-Y",
            titulo = "Prueba",
            tipo = "Transfer",
            fecha = LocalDate.of(2026, 10, 19).toString(),
            hora = "12:10",
            recogida = "Hotel",
            destino = "Destino",
            asignadoA = "DRV-001"
        )
        assertNull(evaluarLlegada(servicio, dentro))
    }

    @Test fun checkInFueraDeGeocercaFalla() {
        val repo = repo()
        repo.iniciarSesion("guia.demo", "Lince123")
        val error = assertFailsWith<IllegalArgumentException> {
            repo.registrarCheckIn("LIN-009", "Hotel Plaza", "", fuera)
        }
        assertTrue(error.message!!.contains("geocerca"))
    }

    @Test fun checkInDentroRegistraUbicacionYCheckoutRegistraDropoff() {
        val repo = repo()
        repo.iniciarSesion("guia.demo", "Lince123")
        repo.registrarCheckIn("LIN-009", "Hotel Plaza", "", dentro)
        val enCurso = repo.obtenerServicioPorId("LIN-009")
        assertNotNull(enCurso.latitudInicio)
        assertNotNull(enCurso.distanciaInicioMetros)
        assertNotNull(enCurso.ultimaLatitud)
        assertTrue(enCurso.distanciaInicioMetros!! < enCurso.radioGeocercaMetros)

        val reporte = Ubicacion(-33.4430, -70.6550, precisionMetros = 5.0)
        repo.reportarUbicacion("LIN-009", reporte)
        val reportado = repo.obtenerServicioPorId("LIN-009")
        assertEquals(-33.4430, reportado.ultimaLatitud)
        assertNotNull(reportado.ultimaUbicacionEn)

        val termino = Ubicacion(-33.4521, -70.6790, precisionMetros = 9.0)
        repo.registrarCheckOut("LIN-009", "Sin novedades", termino, "Terminal de prueba")
        val completado = repo.obtenerServicioPorId("LIN-009")
        assertEquals("Terminal de prueba", completado.ubicacionTermino)
        assertEquals(-33.4521, completado.latitudTermino)
        assertEquals(-33.4521, completado.ultimaLatitud)
    }

    @Test fun reportarUbicacionSoloDuranteServicioActivo() {
        val repo = repo()
        repo.iniciarSesion("guia.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> {
            repo.reportarUbicacion("LIN-009", dentro)
        }
        repo.registrarCheckIn("LIN-009", "Hotel Plaza", "", dentro)
        repo.registrarCheckOut("LIN-009", "", dentro, "Terminal")
        assertFailsWith<IllegalArgumentException> {
            repo.reportarUbicacion("LIN-009", dentro)
        }
    }
}
