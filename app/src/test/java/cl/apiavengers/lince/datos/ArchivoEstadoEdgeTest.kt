package cl.apiavengers.lince.datos

import androidx.test.core.app.ApplicationProvider
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArchivoEstadoEdgeTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun leerJsonConNulosYEnumsInvalidos() {
        val archivo = ArchivoEstado(context)
        context.filesDir.listFiles()?.forEach { if (it.name.startsWith("estado_lince")) it.delete() }
        val servicios = JSONObject()
        val item = JSONObject()
        item.put("estado", "NO_EXISTE")
        item.put("asignadoA", "U3")
        item.put("acompananteId", JSONObject.NULL)
        item.put("respondidoPor", JSONObject.NULL)
        item.put("respondidoEn", JSONObject.NULL)
        item.put("motivoRechazo", JSONObject.NULL)
        item.put("inicioEn", JSONObject.NULL)
        item.put("terminoEn", JSONObject.NULL)
        item.put("inicioPor", JSONObject.NULL)
        item.put("ubicacionInicio", JSONObject.NULL)
        item.put("observaciones", "obs")
        item.put("puntaje", JSONObject.NULL)
        item.put("calificadoPor", JSONObject.NULL)
        item.put("calificadoEn", JSONObject.NULL)
        item.put("reasignadoPor", JSONObject.NULL)
        item.put("reasignadoEn", JSONObject.NULL)
        servicios.put("S3", item)
        val perfiles = JSONObject()
        val p = JSONObject()
        p.put("nombre", "N")
        p.put("correo", "c@c.com")
        p.put("telefono", "123")
        p.put("zona", "Z")
        p.put("idiomas", JSONArray(arrayOf("NO_EXISTE")))
        perfiles.put("U3", p)
        val raiz = JSONObject()
        raiz.put("servicios", servicios)
        raiz.put("perfiles", perfiles)
        java.io.File(context.filesDir, "estado_lince.json").writeText(raiz.toString())
        val leido = archivo.leer()
        assertEquals(cl.apiavengers.lince.modelo.EstadoServicio.PENDIENTE, leido.servicios["S3"]?.estado)
        assertEquals(cl.apiavengers.lince.modelo.Idioma.ESPANOL, leido.perfiles["U3"]?.idiomas?.first())
    }

    @Test
    fun leerSinServiciosNiPerfiles() {
        val archivo = ArchivoEstado(context)
        context.filesDir.listFiles()?.forEach { if (it.name.startsWith("estado_lince")) it.delete() }
        java.io.File(context.filesDir, "estado_lince.json").writeText("{}")
        val leido = archivo.leer()
        assertTrue(leido.servicios.isEmpty())
        assertTrue(leido.perfiles.isEmpty())
    }
}
