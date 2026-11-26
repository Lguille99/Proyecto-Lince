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
    onPrimaryContainer = AzulCorporativoOscuro,
    secondary = AzulCorporativoOscuro,
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
            window.statusBarColor = if (darkTheme) NavyEstructuralOscuro.toArgb() else NavyEstructuralClaro.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EstiloLince,
        content = content
    )
}
