package cl.apiavengers.lince.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.Disponibilidad
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.modelo.Modalidad
import cl.apiavengers.lince.ui.idioma.textos

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
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) {
        ContenidoBoton(
            texto = texto,
            cargando = cargando,
            icono = icono,
            colorCargando = MaterialTheme.colorScheme.onPrimary
        )
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
    Button(
        onClick = onClick,
        enabled = enabled && !cargando,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) {
        ContenidoBoton(
            texto = texto,
            cargando = cargando,
            icono = icono,
            colorCargando = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ContenidoBoton(
    texto: String,
    cargando: Boolean,
    icono: ImageVector?,
    colorCargando: Color
) {
    if (cargando) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = colorCargando
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icono != null) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                texto,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
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
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) {
        ContenidoBoton(
            texto = texto,
            cargando = false,
            icono = icono,
            colorCargando = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
fun ChipSuave(
    texto: String,
    contenedor: Color? = null,
    contenido: Color? = null,
    icono: ImageVector? = null
) {
    val fondo = contenedor ?: MaterialTheme.colorScheme.surfaceContainerHigh
    val tinta = contenido ?: MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = RoundedCornerShape(50),
        color = fondo,
        contentColor = tinta,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icono != null) {
                Icon(icono, contentDescription = texto, tint = tinta, modifier = Modifier.size(14.dp))
            }
            Text(texto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = tinta)
        }
    }
}

@Composable
fun ChipEstadoServicio(estado: EstadoServicio) {
    val (icono, fondo, tinta) = when (estado) {
        EstadoServicio.PENDIENTE -> Triple(
            Icons.Default.DateRange,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        EstadoServicio.CONFIRMADA -> Triple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        EstadoServicio.COMPLETADA -> Triple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        EstadoServicio.RECHAZADA -> Triple(
            Icons.Default.Close,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }
    ChipSuave(textos().estado(estado), contenedor = fondo, contenido = tinta, icono = icono)
}

@Composable
fun ChipDisponibilidad(disponibilidad: Disponibilidad) {
    val (icono, fondo, tinta) = when (disponibilidad) {
        Disponibilidad.DISPONIBLE -> Triple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        Disponibilidad.EN_SERVICIO -> Triple(
            Icons.Default.DateRange,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        Disponibilidad.NO_DISPONIBLE -> Triple(
            Icons.Default.Close,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }
    ChipSuave(textos().disponibilidad(disponibilidad), contenedor = fondo, contenido = tinta, icono = icono)
}

@Composable
fun ChipIdioma(idioma: Idioma) {
    ChipSuave(
        textos().idioma(idioma),
        contenedor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contenido = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ChipModalidad(modalidad: Modalidad) {
    ChipSuave(
        textos().modalidad(modalidad),
        contenedor = MaterialTheme.colorScheme.primaryContainer,
        contenido = MaterialTheme.colorScheme.onPrimaryContainer
    )
}
