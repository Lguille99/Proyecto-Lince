package cl.apiavengers.lince.datos.southbound

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class FechasSouthboundTest {
    @Test
    fun fechaYHoraParseanYFormatean() {
        assertEquals(LocalDate.of(2026, 10, 1), FechasSouthbound.fecha("2026-10-01"))
        assertEquals(LocalTime.of(14, 30), FechasSouthbound.hora("14:30"))
        assertEquals("2026-10-01", FechasSouthbound.formatearFecha(LocalDate.of(2026, 10, 1)))
        assertEquals("14:30", FechasSouthbound.formatearHora(LocalTime.of(14, 30)))
        assertEquals("14:30", FechasSouthbound.formatearHora("14:30"))
        assertEquals("xx", FechasSouthbound.formatearHora("xx"))
    }

    @Test
    fun hoyYAhoraNoNulos() {
        assertNotNull(FechasSouthbound.hoy())
        assertNotNull(FechasSouthbound.ahora())
    }

    @Test
    fun normalizarSelloConOffset() {
        val r = FechasSouthbound.normalizarSello("2026-10-01T14:30:00Z")
        assertEquals(2026, r.year)
    }

    @Test(expected = java.time.format.DateTimeParseException::class)
    fun normalizarSelloInvalidoLanza() {
        FechasSouthbound.normalizarSello("no-es-fecha")
    }
}
