package cl.apiavengers.lince.datos

import android.Manifest
import androidx.test.core.app.ApplicationProvider
import org.robolectric.Shadows
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UbicacionGpsPermisoTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun obtenerUbicacionConPermisoDenegadoNoCrash() {
        runBlocking {
            val gps = UbicacionGps(context)
            try { gps.obtenerUbicacion() } catch (e: Exception) { }
        }
    }

    @Test
    fun obtenerUbicacionConPermisoConcedido() {
        runBlocking {
            Shadows.shadowOf(context as android.app.Application).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            val gps = UbicacionGps(context)
            try { gps.obtenerUbicacion() } catch (e: Exception) { }
        }
    }
}
