package cl.apiavengers.lince.viewmodel

import cl.apiavengers.lince.datos.MonitorConexion
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConexionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun conectadoReflejaMonitor() = runTest {
        val monitor = mockk<MonitorConexion>(relaxed = true)
        every { monitor.conectado } returns flowOf(true)
        val vm = ConexionViewModel(monitor)
        val job = kotlinx.coroutines.GlobalScope.launch { vm.conectado.collect {} }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()
    }
}
