package cl.apiavengers.lince

import cl.apiavengers.lince.datos.AlmacenEstadoMemoria
import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.*

class RepositorioLinceTest {
    private val reloj = Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneOffset.UTC)

    private fun repo(almacen: AlmacenEstadoMemoria = AlmacenEstadoMemoria()) =
        RepositorioLince(almacen, reloj)

    @Test fun exigeSesion() {
        assertFailsWith<IllegalStateException> { repo().consultarServicios() }
    }

    @Test fun conductorVeSoloSusServicios() {
        val repo = repo()
        repo.iniciarSesion("conductor.demo", "Lince123")
        assertEquals(4, repo.consultarServicios().size)
        assertTrue(repo.consultarServicios().all { it.esDe("DRV-001") })
    }

    @Test fun guiaVeSoloSusServicios() {
        val repo = repo()
        repo.iniciarSesion("guia.demo", "Lince123")
        assertEquals(2, repo.consultarServicios().size)
        assertTrue(repo.consultarServicios().all { it.esDe("GUI-002") })
    }

    @Test fun acompananteVeElServicioPeroNoResponde() {
        val repo = repo()
        repo.iniciarSesion("conductor.demo", "Lince123")
        assertTrue(repo.consultarServicios().any { it.id == "LIN-009" && it.acompananteId == "DRV-001" })
        assertFailsWith<IllegalArgumentException> { repo.confirmarServicio("LIN-009") }
        assertFailsWith<IllegalArgumentException> {
            repo.registrarCheckIn("LIN-009", "Hotel Plaza")
        }
    }

    @Test fun coordinadorVeTodosLosActivos() {
        val repo = repo()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        assertEquals(10, repo.consultarServicios().size)
    }

    @Test fun claveIncorrectaRevocaSesionAnterior() {
        val repo = repo()
        repo.iniciarSesion("conductor.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.iniciarSesion("conductor.demo", "incorrecta") }
        assertFailsWith<IllegalStateException> { repo.consultarServicios() }
    }

    @Test fun cerrarSesionBloqueaConsultas() {
        val repo = repo()
        repo.iniciarSesion("guia.demo", "Lince123")
        repo.cerrarSesion()
        assertFailsWith<IllegalStateException> { repo.consultarServicios() }
    }

    @Test fun equipoVisibleTrasSesion() {
        val repo = repo()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        val equipo = repo.consultarEquipo()
        assertEquals(6, equipo.size)
        assertTrue(equipo.any { it.id == "DRV-001" && it.telefono.isNotBlank() })
        assertTrue(equipo.any { it.id == "GUI-002" && it.idiomas.contains(Idioma.INGLES) })
    }

    @Test fun obtenerPersonaPorId() {
        val repo = repo()
        repo.iniciarSesion("conductor.demo", "Lince123")
        val persona = repo.obtenerUsuarioPorId("GUI-002")
        assertEquals("GUI-002", persona.id)
        assertFailsWith<IllegalArgumentException> { repo.obtenerUsuarioPorId("NO-EXISTE") }
    }

    @Test fun obtenerServicioPorIdYFiltrarPorPersona() {
        val repo = repo()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        val servicio = repo.obtenerServicioPorId("LIN-001")
        assertEquals("DRV-001", servicio.asignadoA)
        assertEquals(4, repo.consultarServiciosDe("DRV-001").size)
        assertEquals(2, repo.consultarServiciosDe("DRV-002").size)
    }

    @Test fun historialSoloContieneTerminados() {
        val repo = repo()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        val historial = repo.consultarHistorial()
        assertEquals(6, historial.size)
        assertTrue(historial.all {
            it.estado == EstadoServicio.COMPLETADA || it.estado == EstadoServicio.RECHAZADA
        })
    }

    @Test fun conductorVeSuHistorial() {
        val repo = repo()
        repo.iniciarSesion("conductor.demo", "Lince123")
        val historial = repo.consultarHistorial()
        assertEquals(2, historial.size)
        assertTrue(historial.all { it.asignadoA == "DRV-001" })
    }

    @Test fun historialDePersonaIncluyeRechazos() {
        val repo = repo()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        val deDiego = repo.consultarHistorialDe("DRV-002")
        assertEquals(1, deDiego.size)
        assertEquals(EstadoServicio.RECHAZADA, deDiego.first().estado)
    }

    @Test fun resumenCoordinadorConteo() {
        val repo = repo()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        val resumen = repo.resumenCoordinador()
        assertEquals(10, resumen.serviciosActivos)
        assertEquals(6, resumen.pendientes)
        assertEquals(4, resumen.confirmadas)
        assertEquals(1, resumen.rechazadas)
        assertEquals(0, resumen.enEjecucion)
        assertEquals(6, resumen.personasEquipo)
        assertEquals(4, resumen.personasDisponibles)
        assertEquals(6, resumen.historialTotal)
    }

    @Test fun resumenNoDisponibleParaRecursos() {
        val repo = repo()
        repo.iniciarSesion("conductor.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.resumenCoordinador() }
    }

    @Test fun confirmarRegistraAutorYFecha() {
        val repo = repo()
        repo.iniciarSesion("conductor.demo", "Lince123")
        repo.confirmarServicio("LIN-001")
        val servicio = repo.obtenerServicioPorId("LIN-001")
        assertEquals(EstadoServicio.CONFIRMADA, servicio.estado)
        assertEquals("DRV-001", servicio.respondidoPor)
        assertNotNull(servicio.respondidoEn)
    }

    @Test fun rechazarExigeMotivoYRegistraRespuesta() {
        val repo = repo()
        repo.iniciarSesion("conductor.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.rechazarServicio("LIN-001", "   ") }
        assertFailsWith<IllegalArgumentException> { repo.rechazarServicio("LIN-001", "x".repeat(301)) }
        repo.rechazarServicio("LIN-001", "  Choque de agenda  ")
        val servicio = repo.obtenerServicioPorId("LIN-001")
        assertEquals(EstadoServicio.RECHAZADA, servicio.estado)
        assertEquals("Choque de agenda", servicio.motivoRechazo)
        assertEquals("DRV-001", servicio.respondidoPor)
    }

    @Test fun servicioAjenoNoSePuedeResponder() {
        val repo = repo()
        repo.iniciarSesion("guia.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.confirmarServicio("LIN-001") }
        assertFailsWith<IllegalArgumentException> { repo.rechazarServicio("LIN-001", "No puedo") }
    }

    @Test fun noSeRespondeDosVeces() {
        val repo = repo()
        repo.iniciarSesion("conductor.demo", "Lince123")
        repo.confirmarServicio("LIN-001")
        assertFailsWith<IllegalArgumentException> { repo.confirmarServicio("LIN-001") }
        assertFailsWith<IllegalArgumentException> { repo.rechazarServicio("LIN-001", "Tarde") }
    }

    @Test fun checkInExigeUbicacion() {
        val repo = repo()
        repo.iniciarSesion("guia2.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.registrarCheckIn("LIN-009", "  ") }
    }

    @Test fun checkInSoloDesdeQuinceMinutosAntes() {
        val repo = repo()
        repo.iniciarSesion("conductor.demo", "Lince123")
        val error = assertFailsWith<IllegalArgumentException> {
            repo.registrarCheckIn("LIN-002", "Hotel Plaza")
        }
        assertTrue(error.message!!.contains("15 minutos"))
    }

    @Test fun checkInYCheckOutMueveAlHistorial() {
        val repo = repo()
        repo.iniciarSesion("guia2.demo", "Lince123")
        repo.registrarCheckIn("LIN-009", "Hotel Plaza", "Salida puntual")
        val enCurso = repo.obtenerServicioPorId("LIN-009")
        assertEquals("GUI-003", enCurso.inicioPor)
        assertEquals("Hotel Plaza", enCurso.ubicacionInicio)
        assertTrue(repo.consultarServicios().any { it.id == "LIN-009" })
        repo.registrarCheckOut("LIN-009", "Término sin novedades")
        val terminado = repo.obtenerServicioPorId("LIN-009")
        assertEquals(EstadoServicio.COMPLETADA, terminado.estado)
        assertNotNull(terminado.terminoEn)
        assertFalse(repo.consultarServicios().any { it.id == "LIN-009" })
        assertTrue(repo.consultarHistorial().any { it.id == "LIN-009" })
    }

    @Test fun checkOutSinCheckInFalla() {
        val repo = repo()
        repo.iniciarSesion("guia2.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.registrarCheckOut("LIN-009") }
    }

    @Test fun calificarSoloCompletadosYPorCoordinacion() {
        val repo = repo()
        repo.iniciarSesion("guia.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.calificarServicio("LIN-102", 5) }
        repo.iniciarSesion("coordinador.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.calificarServicio("LIN-001", 5) }
        assertFailsWith<IllegalArgumentException> { repo.calificarServicio("LIN-102", 6) }
        repo.calificarServicio("LIN-102", 5)
        assertEquals(5, repo.obtenerServicioPorId("LIN-102").puntaje)
        assertEquals("COO-003", repo.obtenerServicioPorId("LIN-102").calificadoPor)
    }

    @Test fun reasignarValidaRolEIdioma() {
        val repo = repo()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> { repo.reasignarServicio("LIN-001", "GUI-002") }
        assertFailsWith<IllegalArgumentException> { repo.reasignarServicio("LIN-006", "DRV-002") }
        assertEquals(listOf("DRV-002"), repo.candidatosReasignacion("LIN-001").map { it.id })
        repo.reasignarServicio("LIN-001", "DRV-002")
        val servicio = repo.obtenerServicioPorId("LIN-001")
        assertEquals("DRV-002", servicio.asignadoA)
        assertEquals(EstadoServicio.PENDIENTE, servicio.estado)
        assertEquals("COO-003", servicio.reasignadoPor)
        assertEquals(listOf("DRV-001"), repo.candidatosReasignacion("LIN-001").map { it.id })
    }

    @Test fun gananciasIncluyenConfirmadasYCompletadas() {
        val repo = repo()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        assertEquals(160_000, repo.totalGanado("DRV-001"))
        assertEquals(80_000, repo.totalGanado("GUI-002"))
        assertEquals(0, repo.totalGanado("DRV-002"))
    }

    @Test fun promedioDeCalificaciones() {
        val repo = repo()
        repo.iniciarSesion("coordinador.demo", "Lince123")
        assertEquals(4.5, repo.promedioCalificacion("DRV-001"))
        assertEquals(4.0, repo.promedioCalificacion("GUI-002"))
        assertNull(repo.promedioCalificacion("DRV-002"))
    }

    @Test fun actualizarPerfilValidaYPersiste() {
        val almacen = AlmacenEstadoMemoria()
        val repo = repo(almacen)
        repo.iniciarSesion("conductor.demo", "Lince123")
        assertFailsWith<IllegalArgumentException> {
            repo.actualizarPerfil("Martín", "correo-sin-arroba", "+56 9 1234 5678", "Santiago", listOf(Idioma.ESPANOL))
        }
        assertFailsWith<IllegalArgumentException> {
            repo.actualizarPerfil("Martín", "martin@lince.cl", "+56 9 1234 5678", "Santiago", emptyList())
        }
        val actualizado = repo.actualizarPerfil(
            "Martín Actualizado", "martin@lince.cl", "+56 9 1234 5678", "Santiago Centro",
            listOf(Idioma.ESPANOL, Idioma.INGLES)
        )
        assertEquals("Martín Actualizado", actualizado.nombre)
        val otro = repo(almacen)
        otro.iniciarSesion("conductor.demo", "Lince123")
        assertEquals("martin@lince.cl", otro.obtenerUsuarioPorId("DRV-001").correo)
    }

    @Test fun cambiosSobrevivenNuevaInstancia() {
        val almacen = AlmacenEstadoMemoria()
        val repo = repo(almacen)
        repo.iniciarSesion("conductor.demo", "Lince123")
        repo.confirmarServicio("LIN-001")
        val otro = repo(almacen)
        otro.iniciarSesion("conductor.demo", "Lince123")
        assertEquals(EstadoServicio.CONFIRMADA, otro.obtenerServicioPorId("LIN-001").estado)
    }

    @Test fun agendaDelDiaPorRol() {
        val repo = repo()
        repo.iniciarSesion("conductor2.demo", "Lince123")
        assertEquals(listOf("LIN-010"), repo.consultarAgendaDelDia().map { it.id })
        repo.iniciarSesion("coordinador.demo", "Lince123")
        assertEquals(listOf("LIN-009", "LIN-010"), repo.consultarAgendaDelDia().map { it.id }.sorted())
    }
}
