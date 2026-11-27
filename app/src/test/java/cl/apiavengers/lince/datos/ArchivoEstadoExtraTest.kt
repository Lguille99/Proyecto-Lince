package cl.apiavengers.lince.datos

import androidx.test.core.app.ApplicationProvider
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArchivoEstadoExtraTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun guardarYLeerPerfilYServicioCompleto() {
        val archivo = ArchivoEstado(context)
        context.filesDir.listFiles()?.forEach { if (it.name.startsWith("estado_lince")) it.delete() }
        val cambio = CambioServicio(
            estado = EstadoServicio.COMPLETADA,
            asignadoA = "U2",
            acompananteId = "A1",
            respondidoPor = "U2",
            respondidoEn = "2026-10-01T10:00",
            motivoRechazo = "motivo",
            inicioEn = "2026-10-01T08:00",
            terminoEn = "2026-10-01T09:00",
            inicioPor = "U2",
            ubicacionInicio = "Hotel",
            observaciones = "obs",
            puntaje = 5,
            calificadoPor = "COO",
            calificadoEn = "2026-10-01T10:00",
            reasignadoPor = "COO",
            reasignadoEn = "2026-10-01T10:00"
        )
        val perfil = CambioPerfil("Nombre", "a@b.com", "12345678", "Zona", listOf(Idioma.INGLES))
        val guardado = EstadoGuardado(servicios = mapOf("S2" to cambio), perfiles = mapOf("U2" to perfil))
        archivo.guardar(guardado)
        val leido = archivo.leer()
        assertEquals(EstadoServicio.COMPLETADA, leido.servicios["S2"]?.estado)
        assertEquals("Nombre", leido.perfiles["U2"]?.nombre)
        assertEquals(listOf(Idioma.INGLES), leido.perfiles["U2"]?.idiomas)
    }

    @Test
    fun leerArchivoInvalidoDevuelveVacio() {
        val archivo = ArchivoEstado(context)
        context.filesDir.listFiles()?.forEach { if (it.name.startsWith("estado_lince")) it.delete() }
        val f = java.io.File(context.filesDir, "estado_lince.json")
        f.writeText("{invalido")
        assertEquals(EstadoGuardado(), archivo.leer())
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UbicacionGpsTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun tienePermisoSinPermiso() {
        val gps = UbicacionGps(context)
        assertFalse(gps.tienePermiso())
    }
}
