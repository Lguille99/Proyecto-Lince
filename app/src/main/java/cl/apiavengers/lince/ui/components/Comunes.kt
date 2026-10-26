package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.Disponibilidad
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.modelo.Modalidad

@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cargando: Boolean = false,
    icono: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !cargando,
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            if (icono != null) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(texto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cargando: Boolean = false,
    icono: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !cargando,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            if (icono != null) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(texto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BotonPeligro(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icono: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ChipSuave(
    texto: String,
    color: Color = MaterialTheme.colorScheme.primary,
    icono: ImageVector? = null
) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icono != null) {
                Icon(icono, contentDescription = texto, tint = color, modifier = Modifier.size(14.dp))
            }
            Text(texto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
fun ChipEstadoServicio(estado: EstadoServicio) {
    val (icono, color) = when (estado) {
        EstadoServicio.COMPLETADA -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.secondary
        EstadoServicio.RECHAZADA -> Icons.Default.Close to MaterialTheme.colorScheme.error
        EstadoServicio.CONFIRMADA -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        EstadoServicio.PENDIENTE -> Icons.Default.DateRange to MaterialTheme.colorScheme.primary
    }
    ChipSuave(estado.etiqueta, color, icono)
}

@Composable
fun ChipDisponibilidad(disponibilidad: Disponibilidad) {
    val (icono, color) = when (disponibilidad) {
        Disponibilidad.DISPONIBLE -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        Disponibilidad.EN_SERVICIO -> Icons.Default.DateRange to MaterialTheme.colorScheme.secondary
        Disponibilidad.NO_DISPONIBLE -> Icons.Default.Close to MaterialTheme.colorScheme.error
    }
    ChipSuave(disponibilidad.etiqueta, color, icono)
}

@Composable
fun ChipIdioma(idioma: Idioma) {
    ChipSuave(idioma.etiqueta, MaterialTheme.colorScheme.secondary)
}

@Composable
fun ChipModalidad(modalidad: Modalidad) {
    ChipSuave(modalidad.etiqueta, MaterialTheme.colorScheme.primary)
}

private data class ItemBarra(
    val nombre: String,
    val icono: ImageVector,
    val accion: () -> Unit
)

@Composable
fun BarraInferior(
    esCoordinador: Boolean,
    seleccion: String,
    onInicio: () -> Unit,
    onServicios: () -> Unit,
    onPerfil: () -> Unit,
    onAgenda: () -> Unit = {},
    onEquipo: () -> Unit = {}
) {
    val items = buildList {
        add(ItemBarra("Inicio", Icons.Default.Home, onInicio))
        add(ItemBarra("Servicios", Icons.AutoMirrored.Filled.List, onServicios))
        if (esCoordinador) {
            add(ItemBarra("Equipo", Icons.Default.Person, onEquipo))
        } else {
            add(ItemBarra("Agenda", Icons.Default.DateRange, onAgenda))
        }
        add(ItemBarra("Perfil", Icons.Default.Face, onPerfil))
    }
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icono, contentDescription = item.nombre) },
                label = { Text(item.nombre) },
                selected = seleccion == item.nombre,
                onClick = item.accion
            )
        }
    }
}
