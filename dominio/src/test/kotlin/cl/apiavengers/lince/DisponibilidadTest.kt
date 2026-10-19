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
    private val repo = RepositorioDisponibilidad(almacen, Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneOffset.UTC))
    private val conductor = DatosPrueba.usuarios.first { it.rol == Rol.CONDUCTOR }
    private val guia = DatosPrueba.usuarios.first { it.rol == Rol.GUIA }

    @Test fun bloqueaYDesbloqueaUnDia() {
        assertEquals(setOf(hoy), repo.guardar(conductor, hoy, true))
        assertEquals(emptySet(), repo.guardar(conductor, hoy, false))
    }
    @Test fun conservaOtrosDiasSinDuplicados() {
        repo.guardar(conductor, hoy, true)
        repo.guardar(conductor, hoy.plusDays(1), true)
        repo.guardar(conductor, hoy, true)
        assertEquals(2, repo.consultar(conductor).size)
        assertEquals(setOf(hoy.plusDays(1)), repo.guardar(conductor, hoy, false))
    }
    @Test fun separaLasCuentasYConservaDatosEntreInstancias() {
        repo.guardar(conductor, hoy, true)
        assertTrue(repo.consultar(guia).isEmpty())
        assertEquals(setOf(hoy), RepositorioDisponibilidad(almacen).consultar(conductor))
    }
    @Test fun cambiarPerfilNoPierdeLaAgenda() {
        repo.guardar(conductor, hoy, true)
        assertEquals(setOf(hoy), repo.consultar(conductor.copy(id = "otro", nombre = "Nuevo nombre")))
    }
    @Test fun rechazaFechasPasadas() {
        assertFailsWith<IllegalArgumentException> { repo.guardar(conductor, hoy.minusDays(1), true) }
        assertTrue(repo.consultar(conductor).isEmpty())
    }
    @Test fun exigeSesionYRolDeRecurso() {
        assertFailsWith<IllegalArgumentException> { repo.guardar(null, hoy, true) }
        assertFailsWith<IllegalArgumentException> { repo.consultar(DatosPrueba.usuarios.first { it.rol == Rol.COORDINADOR }) }
    }
    @Test fun informaElFalloSinCambiarLosDatosGuardados() {
        repo.guardar(conductor, hoy, true)
        almacen.falla = true
        assertFailsWith<java.io.IOException> { repo.guardar(conductor, hoy, false) }
        assertEquals(setOf(hoy), repo.consultar(conductor))
    }
}
