package cl.apiavengers.lince.viewmodel

import android.app.Application

import cl.apiavengers.lince.datos.southbound.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SouthboundViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun cliente(): ClienteSouthbound {
        val c = mockk<ClienteSouthbound>(relaxed = true)
        coEvery { c.catalogos() } returns ConsultaResultado(CatalogoSouthbound(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()), false, 1L)
        coEvery { c.servicios(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns ConsultaResultado(PaginaSouthbound(emptyList(), MetaSouthbound(1, 20, 0, 1)), false, 1L)
        coEvery { c.guias(any(), any(), any(), any(), any(), any()) } returns ConsultaResultado(PaginaSouthbound(emptyList(), MetaSouthbound(1, 20, 0, 1)), false, 1L)
        coEvery { c.transportistas(any(), any(), any(), any(), any(), any()) } returns ConsultaResultado(PaginaSouthbound(emptyList(), MetaSouthbound(1, 20, 0, 1)), false, 1L)
        coEvery { c.clientes(any(), any(), any(), any()) } returns ConsultaResultado(PaginaSouthbound(emptyList(), MetaSouthbound(1, 20, 0, 1)), false, 1L)
        coEvery { c.resumen(any(), any(), any()) } returns ConsultaResultado(ResumenSouthbound(null, null, 0, 0, emptyList(), emptyList(), emptyList(), emptyList(), emptyList()), false, 1L)
        coEvery { c.detalle(any()) } returns ConsultaResultado(mockk<ServicioSouthbound>(relaxed = true), false, 1L)
        coEvery { c.guia(any()) } returns ConsultaResultado(mockk<GuiaSouthbound>(relaxed = true), false, 1L)
        coEvery { c.transportista(any()) } returns ConsultaResultado(mockk<TransportistaSouthbound>(relaxed = true), false, 1L)
        coEvery { c.cliente(any()) } returns ConsultaResultado(mockk<ClienteApiSouthbound>(relaxed = true), false, 1L)
        return c
    }

    @Test
    fun iniciarYCargarSecciones() = runTest {
        val vm = SouthboundViewModel(mockk<Application>(relaxed = true), cliente())
        vm.iniciar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        vm.seleccionar(SeccionSouthbound.GUIAS)
        vm.seleccionar(SeccionSouthbound.TRANSPORTISTAS)
        vm.seleccionar(SeccionSouthbound.CLIENTES)
        vm.seleccionar(SeccionSouthbound.RESUMEN)
        vm.seleccionar(SeccionSouthbound.SERVICIOS)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.estado.value)
    }

    @Test
    fun cargarServiciosConFiltros() = runTest {
        val vm = SouthboundViewModel(mockk<Application>(relaxed = true), cliente())
        vm.cargarServicios(2, "q", "CF", "CF", "TRANSFER", "SCL", "2026-10-01", "2026-10-31")
        vm.cargarServicios()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun cargarGuiasTransportistasClientesResumen() = runTest {
        val vm = SouthboundViewModel(mockk<Application>(relaxed = true), cliente())
        vm.cargarGuias(1, "q", "SCL", "ES")
        vm.cargarTransportistas(1, "q", "SCL")
        vm.cargarClientes(1, "q")
        vm.cargarResumen("2026-10-01", "2026-10-02", "SCL")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun detalles() = runTest {
        val vm = SouthboundViewModel(mockk<Application>(relaxed = true), cliente())
        vm.detalleServicio(1)
        vm.detalleGuia(1)
        vm.detalleTransportista(1)
        vm.detalleCliente(1)
        vm.cerrarDetalle()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
    }
}
