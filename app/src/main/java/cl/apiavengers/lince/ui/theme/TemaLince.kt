package cl.apiavengers.lince.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresLince = lightColorScheme(
    primary = Color(0xFF245B78), onPrimary = Color.White,
    secondary = Color(0xFF286B5D), onSecondary = Color.White,
    background = Color(0xFFF6F8FA), onBackground = Color(0xFF172B3A),
    surface = Color(0xFFF6F8FA), onSurface = Color(0xFF172B3A),
    surfaceVariant = Color(0xFFE0EDF2), onSurfaceVariant = Color(0xFF172B3A),
    secondaryContainer = Color(0xFFE0EDF2), onSecondaryContainer = Color(0xFF172B3A),
    error = Color(0xFFBA1A1A)
)

@Composable
fun TemaLince(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColoresLince, content = contenido)
}
