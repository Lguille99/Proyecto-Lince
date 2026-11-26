package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cl.apiavengers.lince.ui.theme.*

@Composable
fun LinceCard(
  contenido: @Composable () -> Unit,
  forma: RoundedCornerShape = FormaTarjeta,
  fondo: Color = Colores.card,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    color = fondo,
    shape = forma,
    content = contenido
  )
}
