package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.ui.theme.*

@Composable
fun MetricSubTile(
  icono: ImageVector,
  valor: String,
  etiqueta: String,
  colorIcono: Color = MaterialTheme.colorScheme.primary,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth().padding(8.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = FormaMetrica,
    content = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          icono,
          contentDescription = null,
          tint = colorIcono,
          modifier = Modifier.size(18.dp)
        )
        Text(text = valor, style = EstiloNumeroKpi.copy(fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface))
        Text(text = etiqueta.uppercase(), style = EstiloSubtituloKpi.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
      }
    }
  )
}

@Composable
fun SoftActionButton(
  texto: String,
  icono: ImageVector,
  enClic: () -> Unit,
  modifier: Modifier = Modifier
) {
  Button(
    onClick = enClic,
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.primary
    ),
    shape = FormaMetrica,
    modifier = modifier.fillMaxWidth().height(44.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        icono,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp)
      )
      Text(text = texto.uppercase(), style = EstiloBotonSecundario.copy(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface))
    }
  }
}
