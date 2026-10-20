package cl.apiavengers.lince

import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.modelo.EstadoServicio
import kotlin.test.*

class RepositorioLinceTest {
    @Test fun exigeSesion() {
        assertFailsWith<IllegalStateException> { RepositorioLince().consultarServicios() }
    }
    @Test fun conductorVeSoloSusServicios() {
        val repo = RepositorioLince()
        repo.iniciarSesion("conductor.demo", "Lince123")
        assertEquals(3, repo.consultarServicios().size)
        assertTrue(repo.consultarServicios().all { it.asignadoA == "DRV-001" })
    }
    @Test fun guiaVeSoloSusServicios() {
        val repo = RepositorioLince()
        repo.iniciarSesion("guia.demo", "Lince123")
        assertEquals(2, repo.consultarServicios().size)
        assertTrue(repo.consultarServicios().all { it.asignadoA == "GUI-002" })
    }
    @Test fun coordinadorVeTodosLosActivos() {
        val repo = RepositorioLince()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        assertEquals(8, repo.consultarServicios().size)
    }
    @Test fun claveIncorrectaRevocaSesionAnterior() {
        val repo = RepositorioLince()
        repo.iniciarSesion("conductor.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.iniciarSesion("conductor.demo", "incorrecta") }
        assertFailsWith<IllegalStateException> { repo.consultarServicios() }
    }
    @Test fun cerrarSesionBloqueaConsultas() {
        val repo = RepositorioLince()
        repo.iniciarSesion("guia.demo", "Lince123")
        repo.cerrarSesion()
        assertFailsWith<IllegalStateException> { repo.consultarServicios() }
    }
    @Test fun equipoVisibleTrasSesion() {
        val repo = RepositorioLince()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        val equipo = repo.consultarEquipo()
        assertEquals(6, equipo.size)
        assertTrue(equipo.any { it.id == "DRV-001" && it.telefono.isNotBlank() })
        assertTrue(equipo.any { it.id == "GUI-002" })
        assertTrue(equipo.any { it.id == "DRV-002" })
    }
    @Test fun obtenerPersonaPorId() {
        val repo = RepositorioLince()
        repo.iniciarSesion("conductor.demo", "Lince123")
        val persona = repo.obtenerUsuarioPorId("GUI-002")
        assertEquals("GUI-002", persona.id)
        assertFailsWith<IllegalArgumentException> { repo.obtenerUsuarioPorId("NO-EXISTE") }
    }
    @Test fun obtenerServicioPorIdYFiltrarPorPersona() {
        val repo = RepositorioLince()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        val servicio = repo.obtenerServicioPorId("LIN-001")
        assertEquals("DRV-001", servicio.asignadoA)
        assertEquals(3, repo.consultarServiciosDe("DRV-001").size)
        assertEquals(1, repo.consultarServiciosDe("DRV-002").size)
    }
    @Test fun historialSoloContieneTerminados() {
        val repo = RepositorioLince()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        val historial = repo.consultarHistorial()
        assertEquals(6, historial.size)
        assertTrue(historial.all {
            it.estado == EstadoServicio.COMPLETADA || it.estado == EstadoServicio.RECHAZADA
        })
    }
    @Test fun conductorVeSuHistorial() {
        val repo = RepositorioLince()
        repo.iniciarSesion("conductor.demo", "Lince123")
        val historial = repo.consultarHistorial()
        assertEquals(2, historial.size)
        assertTrue(historial.all { it.asignadoA == "DRV-001" })
    }
    @Test fun historialDePersonaIncluyeRechazos() {
        val repo = RepositorioLince()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        val deDiego = repo.consultarHistorialDe("DRV-002")
        assertEquals(1, deDiego.size)
        assertEquals(EstadoServicio.RECHAZADA, deDiego.first().estado)
    }
    @Test fun resumenCoordinadorConteo() {
        val repo = RepositorioLince()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        val resumen = repo.resumenCoordinador()
        assertEquals(8, resumen.serviciosActivos)
        assertEquals(5, resumen.pendientes)
        assertEquals(6, resumen.personasEquipo)
        assertEquals(4, resumen.personasDisponibles)
        assertEquals(6, resumen.historialTotal)
    }
    @Test fun resumenNoDisponibleParaRecursos() {
        val repo = RepositorioLince()
        repo.iniciarSesion("conductor.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.resumenCoordinador() }
    }
}
