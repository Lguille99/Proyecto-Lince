package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.apiavengers.lince.ui.theme.*

@Composable
fun LinceInputField(
  etiqueta: String,
  valor: String,
  enCambioValor: (String) -> Unit,
  lugar: String? = null,
  iconoTrailing: ImageVector? = null,
  error: Boolean = false,
  mensajeError: String? = null,
  teclado: KeyboardOptions? = null,
  accionesTeclado: KeyboardActions = KeyboardActions.Default,
  transformacionVisual: VisualTransformation = VisualTransformation.None,
  enabled: Boolean = true,
  singleLine: Boolean = true,
  minLines: Int = 1,
  trailingComposable: (@Composable () -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var texto by remember { mutableStateOf(valor) }
  LaunchedEffect(valor) {
    if (valor != texto) texto = valor
  }
  val bordeActivo = if (error) Colores.error else Colores.primario
  OutlinedTextField(
    value = texto,
    onValueChange = { texto = it; enCambioValor(it) },
    enabled = enabled,
    label = { Text(etiqueta, style = EstiloCuerpoSecundario) },
    placeholder = { lugar?.let { Text(it, style = EstiloCuerpoSecundario) } },
    trailingIcon = {
      if (trailingComposable != null) {
        trailingComposable()
      } else {
        iconoTrailing?.let { Icon(it, contentDescription = null, tint = Colores.primario) }
      }
    },
    singleLine = singleLine,
    minLines = minLines,
    textStyle = EstiloCuerpo,
    visualTransformation = transformacionVisual,
    keyboardActions = accionesTeclado,
    modifier = modifier.fillMaxWidth(),
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = Colores.card,
      unfocusedContainerColor = Colores.card,
      disabledContainerColor = Colores.superficieNeutra,
      focusedBorderColor = bordeActivo,
      unfocusedBorderColor = Colores.bordeInput,
      disabledBorderColor = Colores.bordeCard,
      errorBorderColor = Colores.error,
      focusedTextColor = Colores.textoPrincipal,
      unfocusedTextColor = Colores.textoPrincipal,
      disabledTextColor = Colores.textoSecundario,
      focusedPlaceholderColor = Colores.textoSecundario,
      unfocusedPlaceholderColor = Colores.textoSecundario,
      focusedLeadingIconColor = Colores.primario,
      unfocusedLeadingIconColor = Colores.primario
    ),
    shape = FormaInput,
    keyboardOptions = teclado ?: KeyboardOptions.Default
  )
  if (error && mensajeError != null) {
    Text(text = mensajeError, style = EstiloAlerta, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
  }
}
