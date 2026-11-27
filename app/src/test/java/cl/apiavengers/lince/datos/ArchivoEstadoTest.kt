package cl.apiavengers.lince.datos

import androidx.test.core.app.ApplicationProvider
import cl.apiavengers.lince.modelo.EstadoServicio
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArchivoEstadoTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun leerVacioYGuardarLeer() {
        val archivo = ArchivoEstado(context)
        // clean
        context.filesDir.listFiles()?.forEach { if (it.name.startsWith("estado_lince")) it.delete() }
        assertEquals(EstadoGuardado(), archivo.leer())
        val cambio = CambioServicio(estado = EstadoServicio.CONFIRMADA, asignadoA = "U1", observaciones = "obs")
        val guardado = EstadoGuardado(servicios = mapOf("S1" to cambio))
        archivo.guardar(guardado)
        val leido = archivo.leer()
        assertEquals(EstadoServicio.CONFIRMADA, leido.servicios["S1"]?.estado)
        assertEquals("U1", leido.servicios["S1"]?.asignadoA)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArchivoDisponibilidadTestReal {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun guardarYLeerFechas() {
        val archivo = ArchivoDisponibilidad(context)
        val fechas = setOf(java.time.LocalDate.of(2026, 10, 1), java.time.LocalDate.of(2026, 10, 2))
        archivo.guardar("cuenta@test.com", fechas)
        val leidas = archivo.leer("cuenta@test.com")
        assertEquals(fechas, leidas)
    }

    @Test
    fun leerInexistenteDevuelveVacio() {
        val archivo = ArchivoDisponibilidad(context)
        assertEquals(emptySet<java.time.LocalDate>(), archivo.leer("noexiste@test.com"))
    }
}
