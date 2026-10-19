package cl.apiavengers.lince

import cl.apiavengers.lince.datos.RepositorioLince
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
    @Test fun coordinadorVeTodos() {
        val repo = RepositorioLince()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        assertEquals(5, repo.consultarServicios().size)
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
        assertTrue(equipo.size >= 6)
        assertTrue(equipo.any { it.id == "DRV-001" && it.telefono.isNotBlank() })
        assertTrue(equipo.any { it.id == "GUI-002" })
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
        assertEquals(0, repo.consultarServiciosDe("DRV-002").size)
    }
}
