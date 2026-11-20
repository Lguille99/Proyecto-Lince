package cl.apiavengers.lince.backend

import cl.apiavengers.lince.backend.dominio.Reglas
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReglasTest {
    @Test
    fun `distancia haversine es cero en el mismo punto`() {
        assertEquals(0.0, Reglas.distanciaMetros(-33.4372, -70.6506, -33.4372, -70.6506), 0.001)
    }

    @Test
    fun `distancia de 0001 grados de latitud ronda 111 metros`() {
        val distancia = Reglas.distanciaMetros(-33.4372, -70.6506, -33.4362, -70.6506)
        assertTrue(distancia in 110.0..112.5, "distancia=$distancia")
    }

    @Test
    fun `geocerca de 150 metros acepta 111 y rechaza 222`() {
        assertTrue(Reglas.dentroDeGeocerca(-33.4362, -70.6506, -33.4372, -70.6506, 150))
        assertFalse(Reglas.dentroDeGeocerca(-33.4352, -70.6506, -33.4372, -70.6506, 150))
    }

    @Test
    fun `radio de geocerca es parametrizable`() {
        assertFalse(Reglas.dentroDeGeocerca(-33.4362, -70.6506, -33.4372, -70.6506, 50))
        assertTrue(Reglas.dentroDeGeocerca(-33.4362, -70.6506, -33.4372, -70.6506, 500))
    }

    @Test
    fun `check-in es puntual hasta 15 minutos antes del pickup`() {
        val pickup = Instant.parse("2026-01-01T10:00:00Z")
        assertTrue(Reglas.esPuntual(pickup, Instant.parse("2026-01-01T09:00:00Z")))
        assertTrue(Reglas.esPuntual(pickup, Instant.parse("2026-01-01T09:45:00Z")))
        assertFalse(Reglas.esPuntual(pickup, Instant.parse("2026-01-01T09:45:01Z")))
        assertFalse(Reglas.esPuntual(pickup, Instant.parse("2026-01-01T10:00:00Z")))
    }

    @Test
    fun `tardanza menor a 15 minutos se informa en minutos`() {
        val pickup = Instant.parse("2026-01-01T10:00:00Z")
        assertEquals(0, Reglas.minutosDeTardanza(pickup, Instant.parse("2026-01-01T09:45:00Z")))
        assertEquals(10, Reglas.minutosDeTardanza(pickup, Instant.parse("2026-01-01T09:55:00Z")))
        assertEquals(15, Reglas.minutosDeTardanza(pickup, Instant.parse("2026-01-01T10:00:00Z")))
    }

    @Test
    fun `bloqueo exige 7 dias de anticipacion`() {
        val hoy = LocalDate.of(2026, 11, 19)
        assertFalse(Reglas.anticipacionSuficiente(hoy.plusDays(6), hoy))
        assertTrue(Reglas.anticipacionSuficiente(hoy.plusDays(7), hoy))
        assertTrue(Reglas.anticipacionSuficiente(hoy.plusDays(8), hoy))
    }

    @Test
    fun `parsea instantes con offset y con z`() {
        assertEquals(
            Instant.parse("2026-11-19T12:00:00Z"),
            Reglas.parsearInstante("2026-11-19T09:00:00-03:00")
        )
        assertEquals(
            Instant.parse("2026-11-19T12:00:00Z"),
            Reglas.parsearInstante("2026-11-19T12:00:00Z")
        )
    }
}
