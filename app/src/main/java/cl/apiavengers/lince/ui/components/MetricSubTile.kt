package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
  colorIcono: Color = Colores.primario,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth().padding(8.dp),
    color = Colores.superficieTinted,
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
        Text(text = valor, style = EstiloNumeroKpi.copy(fontSize = 17.sp))
        Text(text = etiqueta.uppercase(), style = EstiloSubtituloKpi.copy(fontSize = 10.sp))
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
      containerColor = Colores.superficieTinted,
      contentColor = Colores.primario
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
        tint = Colores.primario,
        modifier = Modifier.size(18.dp)
      )
      Text(text = texto.uppercase(), style = EstiloBotonSecundario.copy(fontSize = 13.sp))
    }
  }
}
