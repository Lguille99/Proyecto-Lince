package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LinceCard(
    modifier: Modifier = Modifier,
    forma: RoundedCornerShape = RoundedCornerShape(16.dp),
    fondo: Color = Color.Unspecified, // Usa el esquema por defecto
    borde: BorderStroke? = null, // Se define internamente si es nulo
    contenido: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = if (fondo == Color.Unspecified) MaterialTheme.colorScheme.surface else fondo,
        shape = forma,
        border = borde ?: BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentColor = MaterialTheme.colorScheme.onSurface,
        content = contenido
    )
}
