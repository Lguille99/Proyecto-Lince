package cl.apiavengers.lince

import cl.apiavengers.lince.datos.*
import cl.apiavengers.lince.modelo.*
import kotlin.test.*

class RespuestasTest {
    private fun conductor() = RepositorioLince().apply { iniciarSesion("conductor.demo", "Lince123") }

    @Test fun respuestaActualizaListaHistorialYResumen() {
        val almacen = AlmacenServiciosMemoria()
        val repo = RepositorioLince(almacen)
        repo.iniciarSesion("conductor.demo", "Lince123")
        repo.responderServicio("LIN-001", true, "")
        assertEquals(EstadoServicio.CONFIRMADA, repo.obtenerServicioPorId("LIN-001").estado)
        assertNotNull(repo.consultarHistorial().find { it.id == "LIN-001" }?.respondidoEn)
        repo.cerrarSesion()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        assertEquals(4, repo.resumenCoordinador().pendientes)
        assertEquals(7, repo.resumenCoordinador().historialTotal)
    }
    @Test fun rechazarExigeMotivoYNoPermiteRepetirRespuesta() {
        val repo = conductor()
        assertFailsWith<IllegalArgumentException> { repo.responderServicio("LIN-003", false, "  ") }
        assertFailsWith<IllegalArgumentException> { repo.responderServicio("LIN-003", false, "a".repeat(301)) }
        assertEquals(EstadoServicio.PENDIENTE, repo.obtenerServicioPorId("LIN-003").estado)
        repo.responderServicio("LIN-003", false, "  No disponible  ")
        assertEquals("No disponible", repo.obtenerServicioPorId("LIN-003").motivoRechazo)
        assertFailsWith<IllegalArgumentException> { repo.responderServicio("LIN-003", true, "") }
    }
    @Test fun soloResponsableRespondePendientes() {
        val repo = conductor()
        assertFailsWith<IllegalArgumentException> { repo.responderServicio("LIN-005", true, "") }
        assertFailsWith<IllegalArgumentException> { repo.responderServicio("LIN-002", true, "") }
        repo.iniciarSesion("coordinador.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.responderServicio("LIN-001", true, "") }
        repo.cerrarSesion()
        assertFailsWith<IllegalStateException> { repo.responderServicio("LIN-001", true, "") }
    }
    @Test fun perfilValidaYConservaIdentidad() {
        val repo = conductor()
        assertFailsWith<IllegalArgumentException> { repo.actualizarPerfil(" ", "conductor@example.com") }
        assertFailsWith<IllegalArgumentException> { repo.actualizarPerfil("Luis", "correo-invalido") }
        assertFailsWith<IllegalArgumentException> { repo.actualizarPerfil("Luis", "a b@example.com") }
        val perfil = repo.actualizarPerfil(" Luis ", "luis@example.com")
        assertEquals("DRV-001", perfil.id)
        assertEquals(Rol.CONDUCTOR, perfil.rol)
        assertEquals("Luis", perfil.nombre)
        assertEquals(3, repo.consultarServicios().size)
    }
}
