package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.ui.components.*
import cl.apiavengers.lince.ui.fechaCorta
import cl.apiavengers.lince.ui.fechaLargaHoy
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun InicioUserScreen(
    vm: LinceViewModel,
    onNavigateToServicios: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToEquipo: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToHistorial: () -> Unit = {},
    onServicioClick: (String) -> Unit = {}
) {
    val usuario = vm.usuario
    val pendientes = vm.servicios.count { it.estado == EstadoServicio.PENDIENTE }
    val proximo = vm.servicios.sortedWith(compareBy({ it.fecha }, { it.hora })).firstOrNull()
    val deHoy = vm.serviciosDeHoy().filter { !vm.esCoordinador() }

    Scaffold(
        bottomBar = {
            BarraInferior(
                esCoordinador = false,
                seleccion = "Inicio",
                onInicio = {},
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Hola, ${usuario?.nombre ?: "usuario"}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    fechaLargaHoy(),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "TU JORNADA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "$pendientes servicios por revisar",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "Hoy tienes ${deHoy.size} servicio(s) asignado(s).",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (deHoy.isNotEmpty()) {
                Text("Servicios de hoy", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                deHoy.forEach { servicio ->
                    TarjetaResumenServicio(servicio, onClick = { onServicioClick(servicio.id) })
                }
            }

            Text("Próximo servicio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (proximo != null) {
                TarjetaResumenServicio(proximo, onClick = { onServicioClick(proximo.id) })
            } else {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("No tienes servicios activos por ahora.", Modifier.padding(16.dp), fontSize = 14.sp)
                }
            }

            BotonPrincipal(texto = "Ver mis servicios", onClick = onNavigateToServicios)
            BotonSecundario(texto = "Consultar historial", onClick = onNavigateToHistorial)
            BotonSecundario(texto = "Ver equipo (guías y conductores)", onClick = onNavigateToEquipo)

            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Disponibilidad", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Recuerda bloquear con 7 días de anticipación los días en que no puedas trabajar.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onNavigateToAgenda) { Text("Actualizar mi agenda") }
                }
            }
        }
    }
}

@Composable
private fun TarjetaResumenServicio(servicio: cl.apiavengers.lince.modelo.Servicio, onClick: () -> Unit) {
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(
                    "${fechaCorta(servicio.fecha)} · ${servicio.hora}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(servicio.titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${servicio.recogida} → ${servicio.destino}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipEstadoServicio(servicio.estado)
                    ChipModalidad(servicio.modalidad)
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver detalle",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
