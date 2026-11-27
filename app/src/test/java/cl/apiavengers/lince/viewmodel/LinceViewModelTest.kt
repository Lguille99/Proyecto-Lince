package cl.apiavengers.lince.viewmodel

import cl.apiavengers.lince.datos.MonitorConexion
import cl.apiavengers.lince.datos.RepositorioLince
import cl.apiavengers.lince.datos.local.ColaSincronizacion
import cl.apiavengers.lince.datos.preferencias.PreferenciasDataStore
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Ubicacion
import cl.apiavengers.lince.modelo.Usuario
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LinceViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun mocks(): Triple<MonitorConexion, PreferenciasDataStore, ColaSincronizacion> {
        val monitor = mockk<MonitorConexion>(relaxed = true)
        every { monitor.conectado } returns flowOf(true)
        val prefs = mockk<PreferenciasDataStore>(relaxed = true)
        every { prefs.ultimaSincronizacion } returns flowOf(null)
        val cola = mockk<ColaSincronizacion>(relaxed = true)
        return Triple(monitor, prefs, cola)
    }

    @Test
    fun loginLogoutYEstados() = runTest {
        val (monitor, prefs, cola) = mocks()
        val repo = mockk<RepositorioLince>(relaxed = true)
        every { repo.iniciarSesion(any(), any()) } returns mockk<Usuario>(relaxed = true).also {
            every { it.rol } returns cl.apiavengers.lince.modelo.Rol.GUIA
            every { it.id } returns "GUI-002"
        }
        every { repo.consultarServicios() } returns emptyList()
        every { repo.consultarHistorial() } returns emptyList()
        every { repo.consultarEquipo() } returns emptyList()
        every { repo.resumenCoordinador() } returns cl.apiavengers.lince.modelo.ResumenCoordinador(0,0,0,0,0,0,0,0)
        val vm = LinceViewModel(monitor, prefs, cola, { repo }, mainDispatcherRule.testDispatcher)
        vm.iniciarSesion("guia.demo", "Lince123")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.uiState.value.usuario)
        vm.esCoordinador()
        vm.sincronizar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        vm.cerrarSesion()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertNull(vm.uiState.value.usuario)
    }

    @Test
    fun loginError() = runTest {
        val (monitor, prefs, cola) = mocks()
        val repo = mockk<RepositorioLince>(relaxed = true)
        every { repo.iniciarSesion(any(), any()) } throws IllegalArgumentException("bad")
        val vm = LinceViewModel(monitor, prefs, cola, { repo }, mainDispatcherRule.testDispatcher)
        vm.iniciarSesion("x", "y")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)
    }

    @Test
    fun confirmarRechazarCheckInOut() = runTest {
        val (monitor, prefs, cola) = mocks()
        val repo = mockk<RepositorioLince>(relaxed = true)
        val usuario = mockk<Usuario>(relaxed = true).also {
            every { it.rol } returns cl.apiavengers.lince.modelo.Rol.GUIA
            every { it.id } returns "GUI-002"
        }
        every { repo.iniciarSesion(any(), any()) } returns usuario
        every { repo.consultarServicios() } returns emptyList()
        every { repo.consultarHistorial() } returns emptyList()
        every { repo.consultarEquipo() } returns emptyList()
        val vm = LinceViewModel(monitor, prefs, cola, { repo }, mainDispatcherRule.testDispatcher)
        vm.iniciarSesion("guia.demo", "Lince123")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        vm.confirmar("LIN-001")
        vm.rechazar("LIN-001", "motivo valido")
        val ubic = Ubicacion(-33.0, -70.0, precisionMetros = 8.0)
        vm.registrarCheckIn("LIN-001", "Hotel", "", ubic)
        vm.registrarCheckOut("LIN-001", "ok", ubic, "Terminal")
        vm.reportarUbicacion("LIN-001", ubic)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun calificarReasignarPerfil() = runTest {
        val (monitor, prefs, cola) = mocks()
        val repo = mockk<RepositorioLince>(relaxed = true)
        val usuario = mockk<Usuario>(relaxed = true).also {
            every { it.rol } returns cl.apiavengers.lince.modelo.Rol.COORDINADOR
            every { it.id } returns "COO-003"
        }
        every { repo.iniciarSesion(any(), any()) } returns usuario
        every { repo.consultarServicios() } returns emptyList()
        every { repo.consultarHistorial() } returns emptyList()
        every { repo.consultarEquipo() } returns emptyList()
        every { repo.resumenCoordinador() } returns cl.apiavengers.lince.modelo.ResumenCoordinador(0,0,0,0,0,0,0,0)
        every { repo.candidatosReasignacion(any()) } returns emptyList()
        val vm = LinceViewModel(monitor, prefs, cola, { repo }, mainDispatcherRule.testDispatcher)
        vm.iniciarSesion("coordinador.demo", "Lince123")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        vm.calificar("LIN-001", 5)
        vm.reasignar("LIN-001", "DRV-002")
        vm.candidatosReasignacion("LIN-001")
        vm.guardarPerfil("Nombre", "a@b.com", "12345678", "Zona", listOf(Idioma.ESPANOL))
        vm.limpiarMensaje()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun helpersYFiltros() = runTest {
        val (monitor, prefs, cola) = mocks()
        val repo = mockk<RepositorioLince>(relaxed = true)
        val usuario = mockk<Usuario>(relaxed = true).also {
            every { it.rol } returns cl.apiavengers.lince.modelo.Rol.GUIA
            every { it.id } returns "GUI-002"
        }
        every { repo.iniciarSesion(any(), any()) } returns usuario
        every { repo.consultarServicios() } returns emptyList()
        every { repo.consultarHistorial() } returns emptyList()
        every { repo.consultarEquipo() } returns emptyList()
        every { repo.obtenerServicioPorId(any()) } returns mockk<Servicio>(relaxed = true)
        every { repo.obtenerUsuarioPorId(any()) } returns usuario
        every { repo.serviciosDelDiaPara(any()) } returns emptyList()
        every { repo.totalGanado(any()) } returns 0
        every { repo.promedioCalificacion(any()) } returns null
        val vm = LinceViewModel(monitor, prefs, cola, { repo }, mainDispatcherRule.testDispatcher)
        vm.iniciarSesion("guia.demo", "Lince123")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        vm.obtenerPersona("GUI-002")
        vm.obtenerServicio("LIN-001")
        vm.serviciosDe("GUI-002")
        vm.serviciosDeHoy()
        vm.totalGanado("GUI-002")
        vm.promedioCalificacion("GUI-002")
        vm.puedeResponder(mockk<Servicio>(relaxed = true))
        vm.puedeIniciar(mockk<Servicio>(relaxed = true))
        vm.esHoy(mockk<Servicio>(relaxed = true).also { every { it.fecha } returns java.time.LocalDate.now().toString() })
        vm.limpiarMensaje()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
    }
}
