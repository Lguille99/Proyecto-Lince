package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.ui.components.*
import cl.apiavengers.lince.ui.fechaCorta
import cl.apiavengers.lince.ui.fechaLargaHoy
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.ui.theme.Colores
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
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val t = textos()
    val usuario = estado.usuario
    val pendientes = estado.servicios.count { it.estado == EstadoServicio.PENDIENTE }
    val proximo = estado.servicios.sortedWith(compareBy({ it.fecha }, { it.hora })).firstOrNull()
    val deHoy = vm.serviciosDeHoy().filter { !vm.esCoordinador() }

    val itemsBarra = listOf(
        Icons.Default.Home to t.navHome,
        Icons.AutoMirrored.Filled.List to t.navServices,
        Icons.Default.DateRange to t.navAgenda,
        Icons.Default.Face to t.navProfile
    )

    Scaffold(
        bottomBar = {
            LinceFloatingBottomBar(
                items = itemsBarra,
                indiceSeleccionado = 0,
                enSeleccionar = { indice ->
                    when (indice) {
                        1 -> onNavigateToServicios()
                        2 -> onNavigateToAgenda()
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    t.homeGreeting(usuario?.nombre ?: t.homeUserFallback),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    fechaLargaHoy(),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LinceCard(
                fondo = Colores.topBar,
                forma = RoundedCornerShape(20.dp),
                borde = null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        t.homeDay,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.textoBlanco
                    )
                    Text(
                        t.homePending(pendientes),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.textoBlanco
                    )
                    Text(
                        t.homeTodayAssigned(deHoy.size),
                        fontSize = 13.sp,
                        color = Colores.subtituloSobreNavy
                    )
                }
            }

            if (deHoy.isNotEmpty()) {
                Text(t.homeTodayServices, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                deHoy.forEach { servicio ->
                    TarjetaResumenServicio(servicio, onClick = { onServicioClick(servicio.id) })
                }
            }

            Text(t.homeNextService, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (proximo != null) {
                TarjetaResumenServicio(proximo, onClick = { onServicioClick(proximo.id) })
            } else {
                LinceCard(modifier = Modifier.fillMaxWidth()) {
                    EstadoVacio(
                        titulo = t.homeNoActive,
                        mensaje = t.homeNoActiveMessage,
                        icono = Icons.Default.DateRange,
                        accionTexto = t.homeUpdateAgenda,
                        onAccion = onNavigateToAgenda
                    )
                }
            }

            BotonPrincipal(texto = t.homeViewServices, onClick = onNavigateToServicios)
            BotonSecundario(texto = t.homeViewHistory, onClick = onNavigateToHistorial)
            BotonSecundario(texto = t.homeViewTeam, onClick = onNavigateToEquipo)

            LinceCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(t.homeAvailability, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        t.homeAvailabilityHint,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onNavigateToAgenda) { Text(t.homeUpdateAgenda) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaResumenServicio(servicio: cl.apiavengers.lince.modelo.Servicio, onClick: () -> Unit) {
    LinceCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
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
                contentDescription = textos().viewDetails,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
