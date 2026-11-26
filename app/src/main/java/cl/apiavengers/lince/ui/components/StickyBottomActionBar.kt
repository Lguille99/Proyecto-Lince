package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.apiavengers.lince.ui.theme.*

@Composable
fun StickyBottomActionBar(
  alerta: Pair<String, Boolean>? = null,
  accionPrimaria: Pair<String, () -> Unit>? = null,
  accionSecundaria: Pair<String, () -> Unit>? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = Colores.card
  ) {
    Column {
      Surface(
        modifier = Modifier.fillMaxWidth().height(1.dp),
        color = Colores.bordeCard
      ) {}
      Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        alerta?.let { (mensaje, esError) ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              if (esError) Icons.Default.Warning else Icons.Default.Info,
              contentDescription = null,
              tint = if (esError) Colores.error else Colores.exitoTexto,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = mensaje,
              style = if (esError) EstiloAlerta else EstiloCuerpo.copy(color = Colores.exitoTexto)
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          accionPrimaria?.let { (texto, accion) ->
            botonPrimario(texto, accion, Modifier.weight(1f))
          }
          accionSecundaria?.let { (texto, accion) ->
            botonSecundario(texto, accion, Modifier.weight(1f))
          }
        }
      }
    }
  }
}

private @Composable fun botonPrimario(
  texto: String,
  accion: () -> Unit,
  modifier: Modifier
) {
  Button(
    onClick = accion,
    colors = ButtonDefaults.buttonColors(containerColor = Colores.primario),
    shape = FormaBotonBajo,
    modifier = modifier.height(50.dp)
  ) {
    Text(text = texto.uppercase(), style = EstiloBotonPrimario)
  }
}

private @Composable fun botonSecundario(
  texto: String,
  accion: () -> Unit,
  modifier: Modifier
) {
  Button(
    onClick = accion,
    colors = ButtonDefaults.buttonColors(containerColor = Colores.card),
    shape = FormaBotonBajo,
    modifier = modifier.height(50.dp)
  ) {
    Text(text = texto.uppercase(), style = EstiloBotonSecundario)
  }
}
