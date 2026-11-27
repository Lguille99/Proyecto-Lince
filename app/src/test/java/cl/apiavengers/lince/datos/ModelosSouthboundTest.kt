package cl.apiavengers.lince.datos.southbound

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ModelosSouthboundTest {
    @Test
    fun paginaDesdeYObjetoData() {
        val json = """{"data":[{"x":1}],"meta":{"page":1,"pageSize":2,"total":3,"totalPages":2}}"""
        val obj = objetoData("""{"data":{"x":1}}""")
        assertEquals(1, obj.getInt("x"))
        val pagina = paginaDesde(json) { it.getInt("x") }
        assertEquals(1, pagina.meta.page)
        assertEquals(1, pagina.datos.first())
    }

    @Test
    fun textoOpcionalYEnteroOpcional() {
        val j = JSONObject("""{"a":"hola","b":5}""")
        assertEquals("hola", j.textoOpcional("a"))
        assertNull(j.textoOpcional("z"))
        assertEquals(5, j.enteroOpcional("b"))
        assertNull(j.enteroOpcional("z"))
    }
}
