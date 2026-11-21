@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.ui.components.BarraInferior
import cl.apiavengers.lince.ui.components.BarraSuperior
import cl.apiavengers.lince.ui.components.BotonPrincipal
import cl.apiavengers.lince.ui.components.BotonSecundario
import cl.apiavengers.lince.ui.components.ChipDisponibilidad
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.viewmodel.LinceViewModel
import kotlinx.coroutines.delay

@Composable
fun ResumenCoordinacionScreen(
    vm: LinceViewModel,
    onServicios: () -> Unit,
    onHistorial: () -> Unit,
    onEquipo: () -> Unit,
    onPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val t = textos()
    val resumen = estado.resumen
    val snackbar = remember { SnackbarHostState() }
    var sincronizando by remember { mutableStateOf(false) }
    val mensaje = estado.mensaje

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbar.showSnackbar(mensaje)
            vm.limpiarMensaje()
        }
    }
    LaunchedEffect(sincronizando) {
        if (sincronizando) {
            delay(600)
            vm.sincronizar()
            sincronizando = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            BarraSuperior(
                titulo = t.summaryTitle,
                subtitulo = if (estado.modoRemoto) t.coordinationRemote else t.coordinationLocal,
                acciones = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = t.logout)
                    }
                }
            )
        },
        bottomBar = {
            BarraInferior(
                esCoordinador = true,
                seleccion = "Inicio",
                onInicio = {},
                onServicios = onServicios,
                onEquipo = onEquipo,
                onPerfil = onPerfil
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(t.assignmentStatus, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    FilaResumen(
                        valor = resumen?.pendientes ?: 0,
                        etiqueta = t.pendingResponse,
                        contenedor = MaterialTheme.colorScheme.primaryContainer,
                        contenido = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    FilaResumen(
                        valor = resumen?.confirmadas ?: 0,
                        etiqueta = t.confirmedPlural,
                        contenedor = MaterialTheme.colorScheme.secondaryContainer,
                        contenido = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    FilaResumen(
                        valor = resumen?.rechazadas ?: 0,
                        etiqueta = t.rejectedPlural,
                        contenedor = MaterialTheme.colorScheme.errorContainer,
                        contenido = MaterialTheme.colorScheme.onErrorContainer
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

            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                    TextButton(onClick = onEquipo) { Text(t.viewFullTeam) }
                }
            }

            BotonPrincipal(
                texto = if (sincronizando) t.syncing else t.simulateSync,
                onClick = { sincronizando = true },
                cargando = sincronizando
            )
            Text(
                t.lastSimulation(estado.ultimaSincronizacion ?: t.notSynced),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaResumen(valor: Int, etiqueta: String, contenedor: Color, contenido: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = contenedor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).background(contenido, CircleShape))
            Text(valor.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = contenido)
            Text(etiqueta, fontSize = 14.sp, color = contenido)
        }
    }
}
