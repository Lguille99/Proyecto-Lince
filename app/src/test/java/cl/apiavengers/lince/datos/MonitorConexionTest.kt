package cl.apiavengers.lince.datos

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MonitorConexionTest {
    @Test fun informaRedValidadaPerdidaYLimpiaElCallback() = runBlocking {
        val contexto = RuntimeEnvironment.getApplication()
        val gestor = contexto.getSystemService(ConnectivityManager::class.java)
        val sombra = shadowOf(gestor)
        val eventos = Channel<Boolean>(Channel.UNLIMITED)
        val tarea = launch { MonitorConexion(contexto).conectado.collect { eventos.send(it) } }
        withTimeout(5000) { eventos.receive() }
        val callback = sombra.networkCallbacks.single()
        val red = gestor.activeNetwork!!
        val capacidades = NetworkCapabilities()
        shadowOf(capacidades).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        shadowOf(capacidades).addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        callback.onCapabilitiesChanged(red, capacidades)
        assertTrue(withTimeout(5000) { eventos.receive() })
        callback.onLost(red)
        assertFalse(withTimeout(5000) { eventos.receive() })
        tarea.cancelAndJoin()
        assertTrue(sombra.networkCallbacks.isEmpty())
    }
}
