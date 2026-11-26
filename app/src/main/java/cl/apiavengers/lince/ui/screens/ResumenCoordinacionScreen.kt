@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.ui.components.BotonPrincipal
import cl.apiavengers.lince.ui.components.BotonSecundario
import cl.apiavengers.lince.ui.components.ChipDisponibilidad
import cl.apiavengers.lince.ui.components.KpiMetricCard
import cl.apiavengers.lince.ui.components.LinceCard
import cl.apiavengers.lince.ui.components.LinceFloatingBottomBar
import cl.apiavengers.lince.ui.components.LinceTopBar
import cl.apiavengers.lince.ui.components.SoftActionButton
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.ui.selloFechaHora
import cl.apiavengers.lince.ui.theme.Colores
import cl.apiavengers.lince.viewmodel.LinceViewModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ResumenCoordinacionScreen(
    vm: LinceViewModel,
    onServicios: () -> Unit,
    onHistorial: () -> Unit,
    onEquipo: () -> Unit,
    onPerfil: () -> Unit,
    onCerrarSesion: () -> Unit,
    onServicioClick: (String) -> Unit
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val t = textos()
    val resumen = estado.resumen
    val snackbar = remember { SnackbarHostState() }
    val mensaje = estado.mensaje

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbar.showSnackbar(mensaje)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            LinceTopBar(
                titulo = t.summaryTitle,
                subtitulo = t.coordinationLocal,
                accionDerecha = onCerrarSesion,
                iconoDerecha = Icons.AutoMirrored.Filled.ExitToApp
            )
        },
        bottomBar = {
            LinceFloatingBottomBar(
                items = listOf(
                    Icons.Default.Home to t.navHome,
                    Icons.AutoMirrored.Filled.List to t.navServices,
                    Icons.Default.Person to t.navTeam,
                    Icons.Default.Face to t.navProfile
                ),
                indiceSeleccionado = 0,
                enSeleccionar = { i ->
                    when (i) {
                        1 -> onServicios()
                        2 -> onEquipo()
                        3 -> onPerfil()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LinceCard(
                fondo = Colores.superficieNeutra,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(t.assignmentStatus, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    KpiMetricCard(
                        label = t.pendingResponse,
                        valor = (resumen?.pendientes ?: 0).toString(),
                        colorSemantico = MaterialTheme.colorScheme.primary
                    )
                    KpiMetricCard(
                        label = t.confirmedPlural,
                        valor = (resumen?.confirmadas ?: 0).toString(),
                        colorSemantico = MaterialTheme.colorScheme.secondary
                    )
                    KpiMetricCard(
                        label = t.rejectedPlural,
                        valor = (resumen?.rechazadas ?: 0).toString(),
                        colorSemantico = MaterialTheme.colorScheme.error
                    )
                    Text(
                        t.activeSummary(
                            resumen?.serviciosActivos ?: 0,
                            resumen?.enEjecucion ?: 0,
                            resumen?.historialTotal ?: 0
                        ),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            BotonPrincipal(texto = t.viewAllServices, onClick = onServicios)
            BotonSecundario(texto = t.consultHistory, onClick = onHistorial)

            LinceCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t.resourceAvailability, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${resumen?.personasDisponibles ?: 0}/${resumen?.personasEquipo ?: 0}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val disponibles = resumen?.personasDisponibles ?: 0
                    val totalEquipo = resumen?.personasEquipo ?: 0
                    LinearProgressIndicator(
                        progress = { if (totalEquipo > 0) disponibles.toFloat() / totalEquipo else 0f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Text(
                        t.availableNow(disponibles, totalEquipo),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    estado.equipo.take(4).forEach { persona ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(persona.nombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(t.rol(persona.rol), fontSize = 12.sp)
                            }
                            ChipDisponibilidad(persona.disponibilidad)
                        }
                    }
                    SoftActionButton(texto = t.viewFullTeam, icono = Icons.Default.Person, enClic = onEquipo)
                }
            }

            val enEjecucion = remember(estado.servicios) {
                estado.servicios.filter {
                    it.inicioEn != null && it.terminoEn == null
                }
            }
            if (enEjecucion.isNotEmpty()) {
                LinceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(t.lastPositionTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        enEjecucion.forEach { servicio ->
                            val persona = estado.equipo.find { it.id == servicio.asignadoA }
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { onServicioClick(servicio.id) }
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    "${servicio.titulo} · ${persona?.nombre ?: t.unassigned}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                val latitud = servicio.ultimaLatitud
                                val longitud = servicio.ultimaLongitud
                                if (latitud != null && longitud != null) {
                                    Text(
                                        t.lastPositionValue(
                                            String.format(Locale.US, "%.5f", latitud),
                                            String.format(Locale.US, "%.5f", longitud),
                                            servicio.ultimaPrecisionMetros?.roundToInt()
                                                ?.let(t::locationAccuracyValue) ?: "—",
                                            selloFechaHora(servicio.ultimaUbicacionEn)
                                        ),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        t.lastPositionNone,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            BotonPrincipal(
                texto = if (estado.sincronizando) t.syncing else t.syncNow,
                onClick = { vm.sincronizar() },
                cargando = estado.sincronizando
            )
            Text(
                t.lastSync(estado.ultimaSincronizacion ?: t.notSynced),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
