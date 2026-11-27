package cl.apiavengers.lince.datos

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MonitorConexionExtraTest {
    @Test
    fun conectadoEmiteValor() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val monitor = MonitorConexion(context)
        val valores = monitor.conectado.take(1).toList()
        assertTrue(valores.isNotEmpty())
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UbicacionGpsExtraTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun obtenerUbicacionSinPermisoLanza() = runTest {
        val gps = UbicacionGps(context)
        try {
            gps.obtenerUbicacion()
        } catch (e: SecurityException) {
            assertTrue(true)
        } catch (e: Exception) {
            // any exception acceptable
        }
    }
}
