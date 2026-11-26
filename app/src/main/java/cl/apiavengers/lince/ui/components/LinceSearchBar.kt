package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

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
    LaunchedEffect(valor) {
        if (valor != texto) texto = valor
    }
    LaunchedEffect(texto) {
        if (texto != valor) {
            delay(250)
            enCambioValor(texto)
        }
    }
    OutlinedTextField(
        value = texto,
        onValueChange = { texto = it },
        placeholder = { Text(pista, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = { Icon(iconoPrincipal, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            iconoTrailing?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(24.dp)
    )
}
