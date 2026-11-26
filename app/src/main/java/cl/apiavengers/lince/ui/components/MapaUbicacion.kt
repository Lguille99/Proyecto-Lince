package cl.apiavengers.lince.ui.components

import android.content.Context
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.math.cos
import kotlin.math.sin

data class MarcaMapa(
    val latitud: Double,
    val longitud: Double,
    val titulo: String
)

@Composable
fun MapaUbicacion(
    modifier: Modifier = Modifier,
    centroLatitud: Double? = null,
    centroLongitud: Double? = null,
    radioMetros: Int? = null,
    marcadores: List<MarcaMapa> = emptyList(),
    altura: Dp = 260.dp
) {
    val centro = remember(centroLatitud, centroLongitud, marcadores) {
        when {
            centroLatitud != null && centroLongitud != null -> GeoPoint(centroLatitud, centroLongitud)
            marcadores.isNotEmpty() -> GeoPoint(marcadores.first().latitud, marcadores.first().longitud)
            else -> null
        }
    }
    val puntosCirculo = remember(centroLatitud, centroLongitud, radioMetros) {
        if (centroLatitud != null && centroLongitud != null && radioMetros != null && radioMetros > 0) {
            puntosCirculo(centroLatitud, centroLongitud, radioMetros)
        } else {
            emptyList()
        }
    }
    val firma = remember(centro, puntosCirculo, marcadores) {
        "${centro?.latitude},${centro?.longitude},${puntosCirculo.size},${marcadores.size}:${marcadores.hashCode()}"
    }
    val ultimaFirma = remember { arrayOfNulls<String>(1) }
    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
            Configuration.getInstance().userAgentValue = ctx.packageName
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(17.0)
            }
        },
        update = { mapView ->
            if (ultimaFirma[0] != firma) {
                ultimaFirma[0] = firma
                mapView.overlays.clear()
                if (centro != null) {
                    mapView.controller.setCenter(centro)
                    if (puntosCirculo.isNotEmpty()) {
                        val poligono = Polygon(mapView)
                        poligono.points = ArrayList(puntosCirculo)
                        poligono.fillColor = 0x1A2196F3
                        poligono.strokeColor = android.graphics.Color.parseColor("#1976D2")
                        poligono.strokeWidth = 3f
                        mapView.overlays.add(poligono)
                    }
                }
                marcadores.forEach { marca ->
                    val marcador = Marker(mapView)
                    marcador.position = GeoPoint(marca.latitud, marca.longitud)
                    marcador.title = marca.titulo
                    marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(marcador)
                }
                mapView.invalidate()
            }
        },
        onRelease = { it.onDetach() },
        modifier = modifier.fillMaxWidth().height(altura)
    )
}

private fun puntosCirculo(latitud: Double, longitud: Double, radioMetros: Int): List<GeoPoint> {
    val puntos = ArrayList<GeoPoint>(73)
    val latitudRad = Math.toRadians(latitud)
    for (i in 0..72) {
        val angulo = Math.toRadians(i * 5.0)
        val desplazamientoLat = (radioMetros * sin(angulo)) / 111320.0
        val desplazamientoLon = (radioMetros * cos(angulo)) / (111320.0 * cos(latitudRad))
        puntos.add(GeoPoint(latitud + desplazamientoLat, longitud + desplazamientoLon))
    }
    return puntos
}
