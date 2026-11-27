package cl.apiavengers.lince.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

data class LinceStatusColors(
    val confirmada: Color,
    val pendiente: Color,
    val enEspera: Color,
    val cancelada: Color,
    val puntual: Color,
    val tardanza: Color,
    val fueraDeGeocerca: Color,
    val offline: Color
)

val LocalLinceStatusColors = compositionLocalOf<LinceStatusColors> {
    error("LinceStatusColors no provisto")
}

object LinceStatusPalette {
    val light = LinceStatusColors(
        confirmada = Color(0xFF1E3A5F),
        pendiente = Color(0xFF64748B),
        enEspera = Color(0xFF3B6EA5),
        cancelada = Color(0xFFC62828),
        puntual = Color(0xFF1E3A5F),
        tardanza = Color(0xFFB45309),
        fueraDeGeocerca = Color(0xFFC62828),
        offline = Color(0xFF64748B)
    )
    val dark = LinceStatusColors(
        confirmada = Color(0xFF3B6EA5),
        pendiente = Color(0xFF94A3B8),
        enEspera = Color(0xFF8FCEE8),
        cancelada = Color(0xFFCF6679),
        puntual = Color(0xFF3B6EA5),
        tardanza = Color(0xFFF3C56B),
        fueraDeGeocerca = Color(0xFFCF6679),
        offline = Color(0xFF94A3B8)
    )
}

@Composable
fun ProvideLinceStatusColors(darkTheme: Boolean, content: @Composable () -> Unit) {
    val palette = remember(darkTheme) { if (darkTheme) LinceStatusPalette.dark else LinceStatusPalette.light }
    CompositionLocalProvider(LocalLinceStatusColors provides palette, content = content)
}
