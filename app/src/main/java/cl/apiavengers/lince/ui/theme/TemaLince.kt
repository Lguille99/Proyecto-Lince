package cl.apiavengers.lince.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val ColoresLince = lightColorScheme(
    primary = Color(0xFF175873),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE8F3),
    onPrimaryContainer = Color(0xFF06303F),
    secondary = Color(0xFF1F6B58),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2EEE3),
    onSecondaryContainer = Color(0xFF03372A),
    tertiary = Color(0xFF8A5B00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE0A3),
    onTertiaryContainer = Color(0xFF2B1B00),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFE3EEF4),
    onBackground = Color(0xFF102A36),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF102A36),
    surfaceVariant = Color(0xFFE1EDF3),
    onSurfaceVariant = Color(0xFF41535F),
    surfaceTint = Color(0xFF175873),
    surfaceDim = Color(0xFFD3E2EA),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7FAFC),
    surfaceContainer = Color(0xFFEFF5F8),
    surfaceContainerHigh = Color(0xFFE7F0F5),
    surfaceContainerHighest = Color(0xFFDFEAF0),
    outline = Color(0xFF738590),
    outlineVariant = Color(0xFFC3D3DC),
    inverseSurface = Color(0xFF26343B),
    inverseOnSurface = Color(0xFFECF2F5),
    inversePrimary = Color(0xFF8ACBE8),
    scrim = Color(0xFF000000)
)

private val ColoresLinceOscuro = darkColorScheme(
    primary = Color(0xFF8FCEE8),
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF0C4A60),
    onPrimaryContainer = Color(0xFFC9E9F7),
    secondary = Color(0xFF8ED8C1),
    onSecondary = Color(0xFF00382C),
    secondaryContainer = Color(0xFF0B4D3E),
    onSecondaryContainer = Color(0xFFB7F0DE),
    tertiary = Color(0xFFF3C56B),
    onTertiary = Color(0xFF412D00),
    tertiaryContainer = Color(0xFF5C4200),
    onTertiaryContainer = Color(0xFFFFE2A5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0D171D),
    onBackground = Color(0xFFE2EDF2),
    surface = Color(0xFF132128),
    onSurface = Color(0xFFE2EDF2),
    surfaceVariant = Color(0xFF253A44),
    onSurfaceVariant = Color(0xFFB7C9D2),
    surfaceTint = Color(0xFF8FCEE8),
    surfaceDim = Color(0xFF0D171D),
    surfaceBright = Color(0xFF2A3B44),
    surfaceContainerLowest = Color(0xFF081115),
    surfaceContainerLow = Color(0xFF111E25),
    surfaceContainer = Color(0xFF16262E),
    surfaceContainerHigh = Color(0xFF1D3039),
    surfaceContainerHighest = Color(0xFF253A44),
    outline = Color(0xFF8499A4),
    outlineVariant = Color(0xFF33474F),
    inverseSurface = Color(0xFFE2EDF2),
    inverseOnSurface = Color(0xFF26343B),
    inversePrimary = Color(0xFF175873),
    scrim = Color(0xFF000000)
)

private val FormasLince = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TemaLince(
    darkTheme: Boolean = isSystemInDarkTheme(),
    contenido: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = if (darkTheme) ColoresLinceOscuro else ColoresLince,
        shapes = FormasLince,
        content = contenido
    )
}
