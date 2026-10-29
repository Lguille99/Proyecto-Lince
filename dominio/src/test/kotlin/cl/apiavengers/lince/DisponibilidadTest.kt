package cl.apiavengers.lince

import cl.apiavengers.lince.datos.*
import cl.apiavengers.lince.modelo.*
import java.time.*
import kotlin.test.*

class DisponibilidadTest {
    private class Memoria : AlmacenDisponibilidad {
        val datos = mutableMapOf<String, Set<LocalDate>>()
        var falla = false
        override fun leer(cuenta: String) = datos[cuenta].orEmpty()
        override fun guardar(cuenta: String, fechas: Set<LocalDate>) {
            if (falla) throw java.io.IOException("Sin espacio")
            datos[cuenta] = fechas
        }
    }

    private val almacen = Memoria()
    private val hoy = LocalDate.of(2026, 10, 19)
    private val futuro = hoy.plusDays(7)
    private val repo = RepositorioDisponibilidad(
        almacen,
        Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneOffset.UTC)
    )
    private val conductor = DatosPrueba.usuarios.first { it.rol == Rol.CONDUCTOR }
    private val guia = DatosPrueba.usuarios.first { it.rol == Rol.GUIA }

    private fun servicio(fecha: LocalDate, estado: EstadoServicio = EstadoServicio.PENDIENTE) = Servicio(
        id = "TEST-1", titulo = "Prueba", tipo = "Transfer", fecha = fecha.toString(), hora = "09:00",
        recogida = "A", destino = "B", asignadoA = conductor.id, estado = estado
    )

    @Test fun bloqueaYDesbloqueaUnDia() {
        assertEquals(setOf(futuro), repo.guardar(conductor, futuro, true))
        assertEquals(emptySet(), repo.guardar(conductor, futuro, false))
    }

    @Test fun conservaOtrosDiasSinDuplicados() {
        repo.guardar(conductor, futuro, true)
        repo.guardar(conductor, futuro.plusDays(1), true)
        repo.guardar(conductor, futuro, true)
        assertEquals(2, repo.consultar(conductor).size)
        assertEquals(setOf(futuro.plusDays(1)), repo.guardar(conductor, futuro, false))
    }

    @Test fun separaLasCuentasYConservaDatosEntreInstancias() {
        repo.guardar(conductor, futuro, true)
        assertTrue(repo.consultar(guia).isEmpty())
        assertEquals(setOf(futuro), RepositorioDisponibilidad(almacen).consultar(conductor))
    }

    @Test fun cambiarPerfilNoPierdeLaAgenda() {
        repo.guardar(conductor, futuro, true)
        assertEquals(setOf(futuro), repo.consultar(conductor.copy(id = "otro", nombre = "Nuevo nombre")))
    }

    @Test fun rechazaFechasPasadas() {
        assertFailsWith<IllegalArgumentException> { repo.guardar(conductor, hoy.minusDays(1), true) }
        assertTrue(repo.consultar(conductor).isEmpty())
    }

    @Test fun exigeSieteDiasDeAnticipacion() {
        val error = assertFailsWith<IllegalArgumentException> {
            repo.guardar(conductor, hoy.plusDays(6), true)
        }
        assertTrue(error.message!!.contains("7 días"))
        assertEquals(setOf(futuro), repo.guardar(conductor, futuro, true))
    }

    @Test fun noBloqueaUnDiaConServicioAsignado() {
        val error = assertFailsWith<IllegalArgumentException> {
            repo.guardar(conductor, futuro, true, listOf(servicio(futuro)))
        }
        assertTrue(error.message!!.contains("servicio asignado"))
        assertTrue(repo.consultar(conductor).isEmpty())
    }

    @Test fun permiteBloquearSiElServicioFueRechazado() {
        assertEquals(
            setOf(futuro),
            repo.guardar(conductor, futuro, true, listOf(servicio(futuro, EstadoServicio.RECHAZADA)))
        )
    }

    @Test fun exigeSesionYRolDeRecurso() {
        assertFailsWith<IllegalArgumentException> { repo.guardar(null, futuro, true) }
        assertFailsWith<IllegalArgumentException> {
            repo.consultar(DatosPrueba.usuarios.first { it.rol == Rol.COORDINADOR })
        }
    }

    @Test fun informaElFalloSinCambiarLosDatosGuardados() {
        repo.guardar(conductor, futuro, true)
        almacen.falla = true
        assertFailsWith<java.io.IOException> { repo.guardar(conductor, futuro, false) }
        assertEquals(setOf(futuro), repo.consultar(conductor))
    }
}
