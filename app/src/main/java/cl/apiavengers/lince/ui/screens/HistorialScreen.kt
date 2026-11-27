@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.ui.components.ChipEstadoServicio
import cl.apiavengers.lince.ui.components.EstadoVacio
import cl.apiavengers.lince.ui.components.LinceCard
import cl.apiavengers.lince.ui.components.LinceFilterChipRow
import cl.apiavengers.lince.ui.components.LinceFloatingBottomBar
import cl.apiavengers.lince.ui.components.LinceTopBar
import cl.apiavengers.lince.ui.fechaNumerica
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.ui.montoClp
import cl.apiavengers.lince.ui.selloFechaHora
import cl.apiavengers.lince.ui.theme.Colores
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun HistorialScreen(
    vm: LinceViewModel,
    onBack: () -> Unit,
    onServicioClick: (String) -> Unit,
    onNavigateToInicio: () -> Unit = {},
    onNavigateToServicios: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToEquipo: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {}
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val t = textos()
    var filtro by remember { mutableStateOf<EstadoServicio?>(null) }
    val usuario = estado.usuario
    val esCoord = vm.esCoordinador()
    val registros = remember(estado.historial, filtro) {
        estado.historial.filter { filtro == null || it.estado == filtro }
    }

    Scaffold(
        topBar = {
            LinceTopBar(titulo = t.historyTitle, navegarAtras = onBack)
        },
        bottomBar = {
            val indice = 1
            val items = if (esCoord) {
                listOf(
                    Icons.Default.Home to t.navHome,
                    Icons.AutoMirrored.Filled.List to t.navServices,
                    Icons.Default.Person to t.navTeam,
                    Icons.Default.Face to t.navProfile
                )
            } else {
                listOf(
                    Icons.Default.Home to t.navHome,
                    Icons.AutoMirrored.Filled.List to t.navServices,
                    Icons.Default.DateRange to t.navAgenda,
                    Icons.Default.Face to t.navProfile
                )
            }
            LinceFloatingBottomBar(
                items = items,
                indiceSeleccionado = indice,
                enSeleccionar = { i ->
                    when (i) {
                        0 -> onNavigateToInicio()
                        2 -> if (esCoord) onNavigateToEquipo() else onNavigateToAgenda()
                        3 -> onNavigateToPerfil()
                    }
                },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LinceCard(
                fondo = Colores.primario,
                borde = null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (esCoord) t.historyGeneral else t.historyAccumulated,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        if (esCoord) t.historyRegistered(estado.historial.size)
                        else montoClp(usuario?.let { vm.totalGanado(it.id) } ?: 0),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        if (esCoord) t.historyIncludes else t.historyConfirmedCompleted,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            val seleccionadoLabel = when (filtro) {
                null -> t.all
                EstadoServicio.COMPLETADA -> t.historyCompletedFilter
                EstadoServicio.RECHAZADA -> t.historyRejectedFilter
                else -> t.all
            }
            LinceFilterChipRow(
                items = listOf(t.all to 0, t.historyCompletedFilter to 0, t.historyRejectedFilter to 0),
                seleccionado = seleccionadoLabel,
                enSeleccionar = { label ->
                    filtro = when (label) {
                        t.historyCompletedFilter -> EstadoServicio.COMPLETADA
                        t.historyRejectedFilter -> EstadoServicio.RECHAZADA
                        else -> null
                    }
                }
            )

            if (registros.isEmpty()) {
                LinceCard(
                    fondo = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EstadoVacio(
                        titulo = t.historyEmpty,
                        mensaje = t.historyEmptyMessage,
                        icono = Icons.AutoMirrored.Filled.List,
                        accionTexto = if (filtro != null) t.viewAll else null,
                        onAccion = if (filtro != null) ({ filtro = null }) else null
                    )
                }
            }

            registros.forEach { servicio ->
                TarjetaHistorial(
                    servicio = servicio,
                    autor = vm.obtenerPersona(servicio.asignadoA)?.nombre ?: servicio.asignadoA,
                    onClick = { onServicioClick(servicio.id) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaHistorial(servicio: Servicio, autor: String, onClick: () -> Unit) {
    val t = textos()
    LinceCard(
        forma = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${t.id} ${servicio.id}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                ChipEstadoServicio(servicio.estado)
            }
            Text(servicio.titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                "${fechaNumerica(servicio.fecha)} · ${servicio.hora}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(t.responsible(autor), fontSize = 13.sp)
            HorizontalDivider()
            if (servicio.respondidoEn != null) {
                Text(
                    if (servicio.estado == EstadoServicio.RECHAZADA) t.rejectedAt(selloFechaHora(servicio.respondidoEn))
                    else t.confirmedAt(selloFechaHora(servicio.respondidoEn)),
                    fontSize = 12.sp
                )
            }
            servicio.inicioEn?.let { Text("Check-in: ${selloFechaHora(it)}", fontSize = 12.sp) }
            servicio.terminoEn?.let { Text("Check-out: ${selloFechaHora(it)}", fontSize = 12.sp) }
            servicio.puntaje?.let { Text(t.rating("$it/5"), fontSize = 12.sp) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(t.fare, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(montoClp(servicio.tarifa), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
