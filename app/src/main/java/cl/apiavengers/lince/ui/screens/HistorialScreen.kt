@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.ui.components.BarraInferior
import cl.apiavengers.lince.ui.components.BarraSuperior
import cl.apiavengers.lince.ui.components.ChipEstadoServicio
import cl.apiavengers.lince.ui.fechaNumerica
import cl.apiavengers.lince.ui.montoClp
import cl.apiavengers.lince.ui.selloFechaHora
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
    var filtro by remember { mutableStateOf<EstadoServicio?>(null) }
    val registros = vm.historial.filter { filtro == null || it.estado == filtro }
    val usuario = vm.usuario

    Scaffold(
        topBar = {
            BarraSuperior(titulo = "Historial", onBack = onBack)
        },
        bottomBar = {
            BarraInferior(
                esCoordinador = vm.esCoordinador(),
                seleccion = "Servicios",
                onInicio = onNavigateToInicio,
                onServicios = onNavigateToServicios,
                onAgenda = onNavigateToAgenda,
                onEquipo = onNavigateToEquipo,
                onPerfil = onNavigateToPerfil
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (vm.esCoordinador()) "HISTORIAL GENERAL" else "ACUMULADO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        if (vm.esCoordinador()) "${vm.historial.size} servicios registrados"
                        else montoClp(usuario?.let { vm.totalGanado(it.id) } ?: 0),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        if (vm.esCoordinador()) "Incluye completados y rechazados."
                        else "Servicios confirmados y completados.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filtro == null, onClick = { filtro = null }, label = { Text("Todos") })
                FilterChip(
                    selected = filtro == EstadoServicio.COMPLETADA,
                    onClick = { filtro = EstadoServicio.COMPLETADA },
                    label = { Text("Completados") }
                )
                FilterChip(
                    selected = filtro == EstadoServicio.RECHAZADA,
                    onClick = { filtro = EstadoServicio.RECHAZADA },
                    label = { Text("Rechazados") }
                )
            }

            if (registros.isEmpty()) {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Todavía no hay servicios en tu historial.", Modifier.padding(16.dp))
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
    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ID ${servicio.id}",
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
            Text("Responsable: $autor", fontSize = 13.sp)
            HorizontalDivider()
            if (servicio.respondidoEn != null) {
                Text(
                    if (servicio.estado == EstadoServicio.RECHAZADA) "Rechazado: ${selloFechaHora(servicio.respondidoEn)}"
                    else "Confirmado: ${selloFechaHora(servicio.respondidoEn)}",
                    fontSize = 12.sp
                )
            }
            servicio.inicioEn?.let { Text("Check-in: ${selloFechaHora(it)}", fontSize = 12.sp) }
            servicio.terminoEn?.let { Text("Check-out: ${selloFechaHora(it)}", fontSize = 12.sp) }
            servicio.puntaje?.let { Text("Calificación: $it/5", fontSize = 12.sp) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tarifa", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(montoClp(servicio.tarifa), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
