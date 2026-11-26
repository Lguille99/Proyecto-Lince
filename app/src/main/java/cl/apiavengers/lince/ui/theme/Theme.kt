package cl.apiavengers.lince.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val EsquemaLinceClaro = lightColorScheme(
  primary = Colores.primario,
  onPrimary = Colores.textoBlanco,
  primaryContainer = Colores.superficieTinted,
  onPrimaryContainer = Colores.primario,
  secondary = Colores.exito,
  onSecondary = Colores.textoBlanco,
  secondaryContainer = Colores.exitoFondo,
  onSecondaryContainer = Colores.exitoTexto,
  tertiary = Color(0xFF8A5B00),
  onTertiary = Colores.textoBlanco,
  tertiaryContainer = Color(0xFFFFF3D6),
  onTertiaryContainer = Color(0xFF7A5900),
  background = Colores.fondoPantalla,
  onBackground = Colores.textoPrincipal,
  surface = Colores.card,
  onSurface = Colores.textoPrincipal,
  surfaceVariant = Colores.superficieNeutra,
  onSurfaceVariant = Colores.textoSecundario,
  surfaceTint = Colores.primario,
  error = Colores.error,
  onError = Colores.textoBlanco,
  errorContainer = Colores.errorFondo,
  onErrorContainer = Colores.error,
  outline = Colores.bordeCard,
  outlineVariant = Colores.bordeCard,
  surfaceContainerLowest = Colores.card,
  surfaceContainerLow = Color(0xFFF7FAFC),
  surfaceContainer = Colores.superficieNeutra,
  surfaceContainerHigh = Colores.superficieNeutra,
  surfaceContainerHighest = Colores.superficieNeutra,
  inverseSurface = Colores.topBar,
  inverseOnSurface = Colores.textoBlanco,
  inversePrimary = Colores.superficieTinted,
  scrim = Color(0xFF000000)
)

@Composable
fun LinceTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window ?: return@SideEffect
      @Suppress("DEPRECATION")
      window.statusBarColor = Colores.topBar.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
  }
  MaterialTheme(
    colorScheme = if (darkTheme) ColoresLinceOscuro else EsquemaLinceClaro,
    shapes = Shapes(
      small = RoundedCornerShape(8.dp),
      medium = RoundedCornerShape(12.dp),
      large = RoundedCornerShape(16.dp)
    ),
    content = content
  )
}
