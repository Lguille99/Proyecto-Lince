package cl.apiavengers.lince.viewmodel

import cl.apiavengers.lince.datos.local.ColaSincronizacion
import cl.apiavengers.lince.datos.RepositorioDisponibilidad
import cl.apiavengers.lince.modelo.DisponibilidadUiState
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DisponibilidadViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cargarYSeleccionarFecha() = runTest {
        val repo = mockk<RepositorioDisponibilidad>(relaxed = true)
        val cola = mockk<ColaSincronizacion>(relaxed = true)
        coEvery { repo.consultar(any()) } returns emptySet()
        val vm = DisponibilidadViewModel(repo, cola, mainDispatcherRule.testDispatcher)
        val usuario = mockk<Usuario>(relaxed = true)
        vm.cargar(usuario)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        vm.seleccionarFecha(LocalDate.now().plusDays(1))
        vm.cambiarBloqueo(true)
    }

    @Test
    fun guardarBloqueo() = runTest {
        val repo = mockk<RepositorioDisponibilidad>(relaxed = true)
        val cola = mockk<ColaSincronizacion>(relaxed = true)
        coEvery { repo.consultar(any()) } returns emptySet()
        coEvery { repo.guardar(any(), any(), any(), any()) } returns setOf(LocalDate.now().plusDays(8))
        val vm = DisponibilidadViewModel(repo, cola, mainDispatcherRule.testDispatcher)
        val usuario = mockk<Usuario>(relaxed = true)
        vm.cargar(usuario)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        vm.seleccionarFecha(LocalDate.now().plusDays(8))
        vm.cambiarBloqueo(true)
        vm.guardar(emptyList())
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
    }
}
