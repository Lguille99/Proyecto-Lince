package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.apiavengers.lince.ui.theme.*

@Composable
fun LinceCard(
  modifier: Modifier = Modifier,
  forma: RoundedCornerShape = FormaTarjeta,
  fondo: Color = Colores.card,
  borde: BorderStroke? = BorderStroke(1.dp, Colores.bordeCard),
  contenido: @Composable () -> Unit
) {
  Surface(
    modifier = modifier,
    color = fondo,
    shape = forma,
    border = borde,
    content = contenido
  )
}
