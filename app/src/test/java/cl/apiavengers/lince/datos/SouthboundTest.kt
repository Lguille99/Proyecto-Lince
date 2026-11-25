package cl.apiavengers.lince.datos

import androidx.room.Room
import cl.apiavengers.lince.datos.southbound.*
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
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
    private val lista get() = recurso("servicios.json")
    private val detalle get() = recurso("detalle.json")
    private fun recurso(nombre: String) = javaClass.getResource("/southbound/$nombre")!!.readText()
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
        val pagina = ServicioSouthbound.paginaDesdeJson(lista)
        assertEquals(4000, pagina.total)
        assertEquals(2000, pagina.paginas)
        assertEquals(2, pagina.meta.pageSize)
        val servicio = pagina.datos.first()
        assertEquals("SRV-000001", servicio.codigo)
        assertEquals("CF", servicio.estadoServicio)
        assertEquals("CF", servicio.estadoReserva)
        assertEquals("2+0", servicio.pax.texto)
        assertNull(servicio.guia)
        assertNull(servicio.idiomaRequerido)
        val json = JSONObject(detalle).getJSONObject("data")
        json.getJSONObject("asignacion").put("statusPL", "DL")
        assertEquals("DL", ServicioSouthbound.desdeJson(json).statusPL)
        assertEquals("CF", ServicioSouthbound.desdeJson(json).estadoServicio)
        json.getJSONObject("asignacion").put("statusPL", JSONObject.NULL)
        assertNull(ServicioSouthbound.desdeJson(json).statusPL)
    }

    @Test fun enviaLaClaveYPaginaConBusquedaCodificada() = runBlocking {
        val cliente = ClienteSouthbound("clave-prueba", db.consultas()) { ruta, clave ->
            assertEquals("clave-prueba", clave)
            assertEquals("/api/v1/servicios?page=2&pageSize=20&q=Hotel+%26+SCL&estadoServicio=CF", ruta)
            RespuestaHttp(200, lista)
        }
        assertFalse(cliente.servicios(2, " Hotel & SCL ", "CF").esCopia)
    }

    @Test fun construyeRutaConFechasEstadosYPaginacion() = runBlocking {
        val cliente = ClienteSouthbound("clave", db.consultas()) { ruta, _ ->
            assertEquals(
                "/api/v1/servicios?page=3&pageSize=50&q=SCL&estadoServicio=CF&estadoReserva=RQ" +
                    "&statusPL=DL&tipo=TRANSFER&oficina=SCL&fechaDesde=2026-10-01&fechaHasta=2026-10-31&orden=-fecha",
                ruta
            )
            RespuestaHttp(200, lista)
        }
        cliente.servicios(
            3, "SCL", "CF", "RQ", "DL", "TRANSFER", "SCL",
            null, null, null, "2026-10-01", "2026-10-31", "-fecha", 50
        )
        Unit
    }

    @Test fun rechazaFechasInvalidasSinConsultar() = runBlocking {
        val cliente = ClienteSouthbound("clave", db.consultas()) { _, _ -> error("No debe consultar") }
        try {
            cliente.servicios(1, fechaDesde = "01-10-2026")
            fail("Debe rechazar el formato de fecha")
        } catch (error: ErrorSouthbound) {
            assertEquals("validacion", error.codigo)
        }
        try {
            cliente.resumen("2026-10-31", "2026-10-01")
            fail("Debe rechazar el rango invertido")
        } catch (error: ErrorSouthbound) {
            assertEquals("validacion", error.codigo)
        }
    }

    @Test fun interpretaGuiasTransportistasYClientes() {
        val guias = GuiaSouthbound.paginaDesdeJson(recurso("guias.json"))
        assertEquals(45, guias.total)
        assertEquals(2, guias.datos.size)
        assertEquals("GUI-017", guias.datos.first().codigo)
        assertEquals(2, guias.datos.first().idiomas.size)

        val transportistas = TransportistaSouthbound.paginaDesdeJson(recurso("transportistas.json"))
        assertEquals(60, transportistas.total)
        assertEquals("VAN", transportistas.datos.first().vehiculos.first().tipo)
        assertEquals(8, transportistas.datos.first().vehiculos.first().capacidad)

        val clientes = ClienteApiSouthbound.paginaDesdeJson(recurso("clientes.json"))
        assertEquals(40, clientes.total)
        assertEquals("CLI-016", clientes.datos.first().codigo)
        assertEquals("Brasil", clientes.datos.first().pais)
    }

    @Test fun interpretaCatalogoYResumen() {
        val catalogo = CatalogoSouthbound.desdeJson(JSONObject(recurso("catalogos.json")).getJSONObject("data"))
        assertEquals(3, catalogo.oficinas.size)
        assertEquals(listOf("CF", "RQ", "DL", "CX"), catalogo.statusPL.map { it.codigo })
        assertEquals(listOf("TRANSFER", "GUIDED_SERVICE", "EXCURSION"), catalogo.tiposServicio.map { it.tipo })

        val resumen = ResumenSouthbound.desdeJson(JSONObject(recurso("resumen.json")).getJSONObject("data"))
        assertEquals("2026-10-01", resumen.fechaDesde)
        assertEquals(76, resumen.totalServicios)
        assertEquals(402, resumen.totalPax)
        assertEquals("SIN_ASIGNAR", resumen.porStatusPL.last().clave)
        assertEquals(2, resumen.porDia.size)
    }

    @Test fun consultaDetalleDeGuiaConTotalDeServicios() = runBlocking {
        val cliente = ClienteSouthbound("clave", db.consultas()) { ruta, _ ->
            assertEquals("/api/v1/guias/17", ruta)
            RespuestaHttp(200, recurso("guias_detalle.json"))
        }
        assertEquals(23, cliente.guia(17).datos.totalServicios)
    }

    @Test fun consultaCatalogosYResumenConRutaCorrecta() = runBlocking {
        val rutas = mutableListOf<String>()
        val cliente = ClienteSouthbound("clave", db.consultas()) { ruta, _ ->
            rutas += ruta
            if (ruta == "/api/v1/catalogos") RespuestaHttp(200, recurso("catalogos.json"))
            else RespuestaHttp(200, recurso("resumen.json"))
        }
        assertEquals(3, cliente.catalogos().datos.oficinas.size)
        assertEquals(76, cliente.resumen("2026-10-01", "2026-10-02", "SCL").datos.totalServicios)
        assertEquals(
            listOf(
                "/api/v1/catalogos",
                "/api/v1/resumen?fechaDesde=2026-10-01&fechaHasta=2026-10-02&oficina=SCL"
            ),
            rutas
        )
    }

    @Test fun extraeRequestIdYMensajeDelError() = runBlocking {
        val cuerpo = """{"error":{"code":"VALIDATION_ERROR","message":"Parámetros inválidos.",""" +
            """"requestId":"abc-123","details":[{"field":"pageSize","message":"debe ser un entero entre 1 y 100"}]}}"""
        val cliente = ClienteSouthbound("clave", db.consultas()) { _, _ -> RespuestaHttp(400, cuerpo) }
        try {
            cliente.servicios(1)
            fail("Debe propagar el error del servidor")
        } catch (error: ErrorSouthbound) {
            assertEquals("validacion", error.codigo)
            assertEquals("abc-123", error.requestId)
            assertEquals("VALIDATION_ERROR", error.codigoServidor)
            assertEquals("Parámetros inválidos.", error.mensajeServidor)
            assertTrue(error.detalles.first().contains("pageSize"))
        }
    }

    @Test fun conservaLaConsultaAlReabrirYNoMezclaFiltros() = runBlocking {
        ClienteSouthbound("clave", db.consultas()) { _, _ -> RespuestaHttp(200, lista) }
            .servicios(1, "", "CF")
        db.close()
        db = abrir()
        val desconectado = ClienteSouthbound("clave", db.consultas()) { _, _ -> throw IOException() }
        val copia = desconectado.servicios(1, "", "CF")
        assertTrue(copia.esCopia)
        assertEquals(2, copia.datos.datos.size)
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
        assertEquals(2, servidor.servicios(1, "", "").datos.datos.size)
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
