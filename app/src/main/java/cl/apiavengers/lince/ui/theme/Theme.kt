package cl.apiavengers.lince.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EsquemaLinceClaro = lightColorScheme(
    primary = AzulCorporativoClaro,
    onPrimary = Color.White,
    background = CanvasClaro,
    onBackground = TextoPrincipalClaro,
    surface = SuperficieClaro,
    onSurface = TextoPrincipalClaro,
    surfaceVariant = VarianteClaro,
    onSurfaceVariant = TextoSecundarioClaro,
    outline = BordeClaro,
    primaryContainer = VarianteClaro,
    onPrimaryContainer = AzulCorporativoClaro,
    secondary = AzulCorporativoClaro,
    onSecondary = Color.White,
    secondaryContainer = AzulCorporativoClaro,
    onSecondaryContainer = Color.White,
    tertiary = AzulCorporativoClaro,
    onTertiary = Color.White,
    tertiaryContainer = BordeClaro,
    onTertiaryContainer = TextoPrincipalClaro,
    inversePrimary = AzulCorporativoOscuro,
    surfaceTint = AzulCorporativoClaro,
    surfaceDim = BordeClaro,
    surfaceBright = SuperficieClaro,
    surfaceContainerLowest = SuperficieClaro,
    surfaceContainerLow = CanvasClaro,
    surfaceContainer = VarianteClaro,
    surfaceContainerHigh = BordeClaro,
    surfaceContainerHighest = BordeClaro,
    inverseSurface = SuperficieOscuro,
    inverseOnSurface = TextoPrincipalOscuro,
    outlineVariant = BordeClaro,
    error = Color(0xFFB00020)
)

private val EsquemaLinceOscuro = darkColorScheme(
    primary = AzulCorporativoOscuro,
    onPrimary = Color.White,
    background = CanvasOscuro,
    onBackground = TextoPrincipalOscuro,
    surface = SuperficieOscuro,
    onSurface = TextoPrincipalOscuro,
    surfaceVariant = VarianteOscuro,
    onSurfaceVariant = TextoSecundarioOscuro,
    outline = BordeOscuro,
    primaryContainer = VarianteOscuro,
    onPrimaryContainer = TextoPrincipalOscuro,
    secondary = AzulCorporativoOscuro,
    onSecondary = Color.White,
    secondaryContainer = AzulCorporativoClaro,
    onSecondaryContainer = Color.White,
    tertiary = AzulCorporativoOscuro,
    onTertiary = Color.White,
    tertiaryContainer = VarianteOscuro,
    onTertiaryContainer = TextoPrincipalOscuro,
    inversePrimary = AzulCorporativoClaro,
    surfaceTint = AzulCorporativoOscuro,
    surfaceDim = NavyEstructuralOscuro,
    surfaceBright = VarianteOscuro,
    surfaceContainerLowest = NavyEstructuralOscuro,
    surfaceContainerLow = CanvasOscuro,
    surfaceContainer = SuperficieOscuro,
    surfaceContainerHigh = VarianteOscuro,
    surfaceContainerHighest = VarianteOscuro,
    inverseSurface = VarianteClaro,
    inverseOnSurface = TextoPrincipalClaro,
    outlineVariant = BordeOscuro,
    error = Color(0xFFCF6679)
)

@Composable
fun LinceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EsquemaLinceOscuro else EsquemaLinceClaro
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surfaceVariant.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EstiloLince,
        content = content
    )
}
