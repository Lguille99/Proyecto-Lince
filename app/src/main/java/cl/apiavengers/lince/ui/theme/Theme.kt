package cl.apiavengers.lince.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun LinceTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = lightColorScheme(
      primary = Colores.primario,
      onPrimary = Colores.textoBlanco,
      primaryContainer = Colores.superficieTinted,
      secondary = Colores.primario,
      onSecondary = Colores.textoPrincipal,
      secondaryContainer = Colores.superficieNeutra,
      background = Colores.fondoPantalla,
      onBackground = Colores.textoPrincipal,
      surface = Colores.card,
      onSurface = Colores.textoPrincipal,
      surfaceVariant = Colores.superficieNeutra,
      onSurfaceVariant = Colores.textoSecundario,
      error = Colores.error,
      onError = Colores.textoBlanco,
      outline = Colores.bordeCard
    ),
    shapes = Shapes(
      small = RoundedCornerShape(8.dp),
      medium = RoundedCornerShape(12.dp),
      large = RoundedCornerShape(16.dp)
    ),
    content = content
  )
}
