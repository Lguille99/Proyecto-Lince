package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun LinceTopBar(
    titulo: String,
    subtitulo: String? = null,
    navegarAtras: (() -> Unit)? = null,
    accionDerecha: (() -> Unit)? = null,
    iconoDerecha: ImageVector = Icons.Default.MoreVert,
    modifier: Modifier = Modifier
) {
    // Navy estructural dinámico según el brillo del fondo (Modo Claro vs Oscuro)
    val esOscuro = MaterialTheme.colorScheme.background == Color(0xFF0F172A)
    val navyBg = if (esOscuro) Color(0xFF0B1521) else Color(0xFF132333)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = navyBg,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navegarAtras?.let {
                IconButton(onClick = it) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                subtitulo?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            accionDerecha?.let {
                IconButton(onClick = it) {
                    Icon(iconoDerecha, null, tint = Color.White)
                }
            }
        }
    }
}
