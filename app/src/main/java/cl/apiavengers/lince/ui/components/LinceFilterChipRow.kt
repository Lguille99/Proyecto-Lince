package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.apiavengers.lince.ui.theme.*

@Composable
fun LinceFilterChipRow(
  items: List<Pair<String, Int>>,
  seleccionado: String? = null,
  enSeleccionar: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items.forEachIndexed { index, (etiqueta, conteo) ->
      val estaSeleccionado = seleccionado == etiqueta
      val textoChip = "$etiqueta ($conteo)"
      TextButton(
        onClick = { enSeleccionar(etiqueta) },
        colors = ButtonDefaults.textButtonColors(
          containerColor = if (estaSeleccionado) Colores.superficieTinted else Color.Transparent,
          contentColor = if (estaSeleccionado) Colores.primario else Colores.textoPrincipal
        ),
        shape = FormaChip,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
      ) {
        Text(text = textoChip, style = EstiloChip.copy(color = Colores.textoPrincipal))
      }
    }
  }
}
