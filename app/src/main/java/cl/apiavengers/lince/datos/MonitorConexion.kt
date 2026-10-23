package cl.apiavengers.lince.datos

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class MonitorConexion(context: Context) {
    private val gestor = context.applicationContext.getSystemService(ConnectivityManager::class.java)
    val conectado = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            }
            override fun onLost(network: Network) { trySend(false) }
        }
        gestor.registerDefaultNetworkCallback(callback)
        val capacidades = gestor.getNetworkCapabilities(gestor.activeNetwork)
        trySend(capacidades?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true &&
            capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        awaitClose { gestor.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
