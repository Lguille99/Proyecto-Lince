package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.apiavengers.lince.ui.theme.*

@Composable
fun KpiMetricCard(
  label: String,
  valor: String,
  unidad: String? = null,
  subtitulo: String? = null,
  icono: ImageVector? = null,
  colorSemantico: Color = Colores.primario,
  modifier: Modifier = Modifier
) {
  LinceCard(modifier = modifier, contenido = {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = label, style = EstiloMicroHeader, color = colorSemantico)
        icono?.let {
          Icon(
            imageVector = it,
            contentDescription = null,
            tint = colorSemantico,
            modifier = Modifier.size(18.dp)
          )
        }
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = valor, style = EstiloNumeroKpi)
        unidad?.let { Text(text = " $it", style = EstiloUnidadKpi) }
      }
      subtitulo?.let { Text(text = it, style = EstiloSubtituloKpi) }
    }
  })
}
