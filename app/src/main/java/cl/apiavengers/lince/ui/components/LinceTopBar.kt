package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.apiavengers.lince.ui.theme.*

@Composable
fun LinceTopBar(
  titulo: String,
  subtitulo: String? = null,
  navegarAtras: (() -> Unit)? = null,
  accionDerecha: (() -> Unit)? = null,
  iconoDerecha: ImageVector = Icons.Default.MoreVert,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Surface(
      modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
      color = Colores.topBar
    ) {
      Row(
        modifier = Modifier.weight(1f, fill = false).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        navegarAtras?.let {
          IconButton(onClick = it) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Colores.textoBlanco)
          }
        }
        Column(modifier = Modifier.weight(1f, fill = false)) {
          Text(text = titulo, style = EstiloTituloPantalla)
          subtitulo?.let { Text(text = it, style = EstiloSubtituloPantalla) }
        }
      }
      accionDerecha?.let {
        IconButton(onClick = it) {
          Icon(iconoDerecha, null, tint = Colores.textoBlanco)
        }
      }
    }
  }
}
