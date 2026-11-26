package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import cl.apiavengers.lince.ui.theme.*

@Composable
fun LinceSearchBar(
  valor: String,
  enCambioValor: (String) -> Unit,
  pista: String = "Buscar\u2026",
  iconoPrincipal: ImageVector = Icons.Default.Search,
  iconoTrailing: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var texto by remember { mutableStateOf(valor) }
  OutlinedTextField(
    value = texto,
    onValueChange = { texto = it; enCambioValor(it) },
    placeholder = { Text(pista, style = EstiloCuerpoSecundario) },
    leadingIcon = { Icon(iconoPrincipal, contentDescription = null, tint = Colores.primario) },
    trailingIcon = {
      iconoTrailing?.let {
        IconButton(onClick = it) {
          Icon(Icons.Default.Close, contentDescription = null, tint = Colores.iconoInactivo)
        }
      }
    },
    singleLine = true,
    textStyle = EstiloCuerpo,
    modifier = modifier.fillMaxWidth(),
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = Colores.card,
      unfocusedContainerColor = Colores.card,
      focusedBorderColor = Colores.primario,
      unfocusedBorderColor = Colores.bordeInput,
      focusedTextColor = Colores.textoPrincipal,
      unfocusedTextColor = Colores.textoPrincipal,
      focusedPlaceholderColor = Colores.textoSecundario,
      unfocusedPlaceholderColor = Colores.textoSecundario,
      focusedLeadingIconColor = Colores.primario,
      unfocusedLeadingIconColor = Colores.primario
    ),
    shape = FormaInput
  )
}
