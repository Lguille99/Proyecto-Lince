package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

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
    
    OutlinedTextField(
        value = texto,
        onValueChange = { texto = it; enCambioValor(it) },
        enabled = enabled,
        label = { Text(etiqueta) },
        placeholder = { lugar?.let { Text(it) } },
        trailingIcon = {
            if (trailingComposable != null) {
                trailingComposable()
            } else {
                iconoTrailing?.let { Icon(it, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            }
        },
        singleLine = singleLine,
        minLines = minLines,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        visualTransformation = transformacionVisual,
        keyboardOptions = teclado ?: KeyboardOptions.Default,
        keyboardActions = accionesTeclado,
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(12.dp)
    )
    if (error && mensajeError != null) {
        Text(
            text = mensajeError,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
        )
    }
}
