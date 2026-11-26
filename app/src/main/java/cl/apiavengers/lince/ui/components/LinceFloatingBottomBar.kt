package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.ui.theme.*

@Composable
fun LinceFloatingBottomBar(
  items: List<Pair<ImageVector, String>>,
  indiceSeleccionado: Int,
  enSeleccionar: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .height(64.dp),
    color = Colores.card,
    shape = FormaBarraFlotante
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      items.forEachIndexed { index, (icono, etiqueta) ->
        val activo = index == indiceSeleccionado
        IconButton(onClick = { enSeleccionar(index) }) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              icono,
              contentDescription = null,
              tint = if (activo) Colores.primario else Colores.iconoInactivo,
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = etiqueta,
              style = EstiloChip.copy(
                fontSize = 10.sp,
                color = if (activo) Colores.primario else Colores.iconoInactivo
              )
            )
          }
        }
      }
    }
  }
}
