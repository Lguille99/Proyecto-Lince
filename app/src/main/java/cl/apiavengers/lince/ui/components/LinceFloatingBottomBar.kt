package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
    shape = FormaBarraFlotante,
    shadowElevation = 6.dp
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      items.forEachIndexed { index, (icono, etiqueta) ->
        val activo = index == indiceSeleccionado
        Column(
          modifier = Modifier.weight(1f).clickable { enSeleccionar(index) }.padding(vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            icono,
            contentDescription = etiqueta,
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
