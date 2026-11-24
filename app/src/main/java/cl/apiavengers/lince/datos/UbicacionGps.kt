package cl.apiavengers.lince.datos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import cl.apiavengers.lince.modelo.Ubicacion
import java.time.LocalDateTime
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class UbicacionGps(private val context: Context) {
    fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    suspend fun obtenerUbicacion(): Ubicacion {
        if (!tienePermiso()) throw SecurityException("Permiso de ubicación denegado.")
        val gestor = context.getSystemService(LocationManager::class.java)
            ?: throw IllegalStateException("El GPS no está disponible en este dispositivo.")
        val proveedores = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { gestor.isProviderEnabled(it) }.getOrDefault(false) }
        val ultima = ultimaConocida(gestor, proveedores.ifEmpty {
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        })
        if (proveedores.isEmpty()) {
            return ultima?.aModelo()
                ?: throw IllegalStateException("Activa la ubicación (GPS) para continuar.")
        }
        val fresca = withTimeoutOrNull(12_000) { esperarUbicacion(gestor, proveedores) }
        return fresca ?: ultima?.aModelo()
        ?: throw IllegalStateException(
            "No se pudo obtener la ubicación. Intenta nuevamente al aire libre."
        )
    }

    @SuppressLint("MissingPermission")
    private fun ultimaConocida(gestor: LocationManager, proveedores: List<String>): Location? =
        proveedores.mapNotNull { proveedor ->
            runCatching { gestor.getLastKnownLocation(proveedor) }.getOrNull()
        }.maxByOrNull { it.time }

    private suspend fun esperarUbicacion(
        gestor: LocationManager,
        proveedores: List<String>
    ): Ubicacion = suspendCancellableCoroutine { continuacion ->
        val escucha = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (continuacion.isActive) {
                    gestor.removeUpdates(this)
                    continuacion.resume(location.aModelo())
                }
            }

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) = Unit

            @Deprecated("Método obsoleto desde Android 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }
        continuacion.invokeOnCancellation { gestor.removeUpdates(escucha) }
        try {
            proveedores.forEach { proveedor ->
                gestor.requestLocationUpdates(
                    proveedor,
                    0L,
                    0f,
                    escucha,
                    Looper.getMainLooper()
                )
            }
        } catch (error: SecurityException) {
            gestor.removeUpdates(escucha)
            continuacion.resumeWithException(error)
        }
    }
}

private fun Location.aModelo(): Ubicacion = Ubicacion(
    latitud = latitude,
    longitud = longitude,
    precisionMetros = if (hasAccuracy()) accuracy.toDouble() else null,
    obtenidaEn = LocalDateTime.now().withNano(0).toString()
)
