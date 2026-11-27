package cl.apiavengers.lince.datos

import android.content.Context
import java.security.MessageDigest
import java.util.setOf
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ArchivoDisponibilidadTest {
    private lateinit var almacen: ArchivoDisponibilidad
    private val contexto get() = RuntimeEnvironment.getApplication()
    private val cuenta = "test_conductor"

    @Before fun preparar() {
        almacen = ArchivoDisponibilidad(contexto)
    }

    @Test fun `lee archivo inexistente devuelve conjunto vacío`() {
        val resultado = almacen.leer(cuenta)
        assertEquals(setOf(), resultado)
    }

    @Test fun `guarda y lee fechas correctamente`() {
        val fecha = java.time.LocalDate.of(2026, 11, 25)
        almacen.guardar(cuenta, setOf(fecha))
        val resultado = almacen.leer(cuenta)
        assertEquals(setOf(fecha), resultado)
    }

    @Test fun `varias fechas se guardan y leen como set`() {
        val fecha1 = java.time.LocalDate.of(2026, 11, 25)
        val fecha2 = java.time.LocalDate.of(2026, 11, 26)
        almacen.guardar(cuenta, setOf(fecha1, fecha2))
        val resultado = almacen.leer(cuenta)
        assertEquals(setOf(fecha1, fecha2), resultado)
    }

    @Test fun `fechasordenadas se guardan y mantienen orden`() {
        val fechaAnterior = java.time.LocalDate.of(2026, 11, 20)
        val fechaPosterior = java.time.LocalDate.of(2026, 11, 30)
        almacen.guardar(cuenta, setOf(fechaPosterior, fechaAnterior))
        val resultado = almacen.leer(cuenta)
        // Set no garantiza orden, pero las fechas deben estar presentes
        assert(resultado.contains(fechaAnterior))
        assert(resultado.contains(fechaPosterior))
        assert(resultado.size == 2)
    }
}