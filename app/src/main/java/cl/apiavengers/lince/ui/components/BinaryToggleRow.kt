package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.apiavengers.lince.ui.theme.*

@Composable
fun BinaryToggleRow(
  opcionA: String,
  opcionB: String,
  seleccion: Boolean?,
  enSeleccionar: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val seleccionA = seleccion == true
  val seleccionB = seleccion == false
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    opcionBoton(opcionA, seleccionada = seleccionA, accion = { enSeleccionar(true) },
      icono = if (seleccionA) Icons.Default.Check else Icons.Default.Close)
    opcionBoton(opcionB, seleccionada = seleccionB, accion = { enSeleccionar(false) },
      icono = if (seleccionB) Icons.Default.Check else Icons.Default.Close)
  }
}

private @Composable fun opcionBoton(
  etiqueta: String,
  seleccionada: Boolean,
  accion: () -> Unit,
  icono: ImageVector
) {
  Button(
    onClick = accion,
    colors = ButtonDefaults.buttonColors(
      containerColor = if (seleccionada) Colores.exito else Colores.superficieNeutra,
      contentColor = if (seleccionada) Colores.textoBlanco else Colores.iconoInactivo
    ),
    shape = FormaToggle,
    modifier = Modifier.fillMaxWidth().height(48.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        icono,
        contentDescription = null,
        tint = if (seleccionada) Colores.textoBlanco else Colores.iconoInactivo,
        modifier = Modifier.size(18.dp)
      )
      Text(
        text = etiqueta.uppercase(),
        style = EstiloChip.copy(
          color = if (seleccionada) Colores.textoBlanco else Colores.iconoInactivo
        )
      )
    }
  }
}
