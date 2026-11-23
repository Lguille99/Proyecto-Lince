package cl.apiavengers.lince.datos

import androidx.room.Room
import cl.apiavengers.lince.datos.southbound.*
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SouthboundTest {
    private lateinit var db: CacheSouthbound
    private val contexto get() = RuntimeEnvironment.getApplication()
    private val lista get() = javaClass.getResource("/southbound/servicios.json")!!.readText()
    private val detalle get() = javaClass.getResource("/southbound/detalle.json")!!.readText()
    private fun abrir() = Room.databaseBuilder(contexto, CacheSouthbound::class.java, "southbound-test.db")
        .allowMainThreadQueries().build()

    @Before fun preparar() {
        contexto.deleteDatabase("southbound-test.db")
        db = abrir()
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase("southbound-test.db")
    }

    @Test fun interpretaLaRespuestaRealYConservaEstadosSeparados() {
        val pagina = PaginaSouthbound.desdeJson(lista)
        assertEquals(4000, pagina.total)
        assertEquals(2000, pagina.paginas)
        val servicio = pagina.servicios.first()
        assertEquals("SRV-000001", servicio.codigo)
        assertEquals("CF", servicio.estadoServicio)
        assertEquals("CF", servicio.estadoReserva)
        assertNull(servicio.guia)
        assertNull(servicio.idioma)
        val json = org.json.JSONObject(detalle).getJSONObject("data")
        json.getJSONObject("asignacion").put("statusPL", "DL")
        assertEquals("DL", ServicioSouthbound.desdeJson(json).estadoAsignacion)
        assertEquals("CF", ServicioSouthbound.desdeJson(json).estadoServicio)
        json.getJSONObject("asignacion").put("statusPL", org.json.JSONObject.NULL)
        assertNull(ServicioSouthbound.desdeJson(json).estadoAsignacion)
    }

    @Test fun enviaLaClaveYPaginaConBusquedaCodificada() = runBlocking {
        val cliente = ClienteSouthbound("clave-prueba", db.consultas()) { ruta, clave ->
            assertEquals("clave-prueba", clave)
            assertEquals("/api/v1/servicios?page=2&pageSize=20&q=Hotel+%26+SCL&estadoServicio=CF", ruta)
            RespuestaHttp(200, lista)
        }
        assertFalse(cliente.servicios(2, " Hotel & SCL ", "CF").esCopia)
    }

    @Test fun conservaLaConsultaAlReabrirYNoMezclaFiltros() = runBlocking {
        ClienteSouthbound("clave", db.consultas()) { _, _ -> RespuestaHttp(200, lista) }
            .servicios(1, "", "CF")
        db.close()
        db = abrir()
        val desconectado = ClienteSouthbound("clave", db.consultas()) { _, _ -> throw IOException() }
        val copia = desconectado.servicios(1, "", "CF")
        assertTrue(copia.esCopia)
        assertEquals(2, copia.datos.servicios.size)
        assertTrue(copia.guardadoEn > 0)
        try {
            desconectado.servicios(1, "otro", "CF")
            fail("No debe mostrar la copia de otro filtro")
        } catch (error: ErrorSouthbound) { assertEquals("conexion", error.codigo) }
    }

    @Test fun unaClaveInvalidaNoQuedaOcultaPorLaCopia() = runBlocking {
        ClienteSouthbound("clave", db.consultas()) { _, _ -> RespuestaHttp(200, lista) }.servicios(1, "", "")
        for (codigo in listOf(401, 403)) {
            try {
                ClienteSouthbound("clave", db.consultas()) { _, _ -> RespuestaHttp(codigo, "") }.servicios(1, "", "")
                fail("Debe avisar del problema de autorización")
            } catch (error: ErrorSouthbound) { assertEquals("clave", error.codigo) }
        }
    }

    @Test fun fallaServidorUsaCopiaYFormatoIncorrectoNoLaReemplaza() = runBlocking {
        ClienteSouthbound("clave", db.consultas()) { _, _ -> RespuestaHttp(200, lista) }.servicios(1, "", "")
        val servidor = ClienteSouthbound("clave", db.consultas()) { _, _ -> RespuestaHttp(503, "") }
        assertTrue(servidor.servicios(1, "", "").esCopia)
        try {
            ClienteSouthbound("clave", db.consultas()) { _, _ -> RespuestaHttp(200, "{}") }.servicios(1, "", "")
            fail("Debe rechazar un contrato inválido")
        } catch (error: ErrorSouthbound) { assertEquals("formato", error.codigo) }
        assertEquals(2, servidor.servicios(1, "", "").datos.servicios.size)
    }

    @Test fun consultaDetallePorIdYLoRecuperaSinConexion() = runBlocking {
        val cliente = ClienteSouthbound("clave", db.consultas()) { ruta, _ ->
            assertEquals("/api/v1/servicios/1", ruta)
            RespuestaHttp(200, detalle)
        }
        assertEquals("SRV-000001", cliente.detalle(1).datos.codigo)
        val offline = ClienteSouthbound("clave", db.consultas()) { _, _ -> throw IOException() }
        assertEquals("SRV-000001", offline.detalle(1).datos.codigo)
        assertTrue(offline.detalle(1).esCopia)
    }

    @Test fun faltaClaveNoHacePeticiones() = runBlocking {
        val cliente = ClienteSouthbound("", db.consultas()) { _, _ -> error("No debe consultar") }
        try {
            cliente.servicios(1, "", "")
            fail("Debe pedir configuración")
        } catch (error: ErrorSouthbound) { assertEquals("configuracion", error.codigo) }
    }

    @Test fun muestraErrorCuandoElServicioNoExiste() = runBlocking {
        val cliente = ClienteSouthbound("clave", db.consultas()) { _, _ -> RespuestaHttp(404, "") }
        try {
            cliente.detalle(987654)
            fail("Debe informar que no existe")
        } catch (error: ErrorSouthbound) { assertEquals("no_encontrado", error.codigo) }
    }
}
