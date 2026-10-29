@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.ui.components.BarraInferior
import cl.apiavengers.lince.ui.components.BarraSuperior
import cl.apiavengers.lince.ui.components.BotonPrincipal
import cl.apiavengers.lince.ui.components.ChipEstadoServicio
import cl.apiavengers.lince.ui.fechaNumerica
import cl.apiavengers.lince.ui.selloFechaHora
import cl.apiavengers.lince.viewmodel.LinceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun EjecucionScreen(
    vm: LinceViewModel,
    servicioId: String,
    onBack: () -> Unit,
    onPersonaClick: (String) -> Unit,
    onNavigateToInicio: () -> Unit = {},
    onNavigateToServicios: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToEquipo: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {}
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val servicio = remember(estado.servicios, estado.historial) { vm.obtenerServicio(servicioId) }
    val snackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    var ubicacion by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var ahora by remember { mutableStateOf(LocalDateTime.now()) }
    var enviando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            ahora = LocalDateTime.now()
        }
    }
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
            BarraSuperior(titulo = "Ejecución del servicio", onBack = onBack)
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
        if (servicio == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No se encontró el servicio.")
            }
            return@Scaffold
        }
        val titular = remember(estado.equipo) { vm.obtenerPersona(servicio.asignadoA) }
        val inicioPermitido = servicio.inicioProgramado.minusMinutes(15)
        val puedeIniciar = servicio.estado == EstadoServicio.CONFIRMADA &&
            servicio.inicioEn == null && !ahora.isBefore(inicioPermitido)
        val puedeTerminar = servicio.inicioEn != null && servicio.terminoEn == null

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChipEstadoServicio(servicio.estado)
                Text("ID ${servicio.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(servicio.titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "${fechaNumerica(servicio.fecha)} · ${servicio.hora}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "${servicio.recogida} → ${servicio.destino}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Registro de hitos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Hito(
                        titulo = "Check-in",
                        detalle = servicio.inicioEn?.let {
                            "${selloFechaHora(it)} · ${servicio.ubicacionInicio.orEmpty()}"
                        } ?: "Pendiente",
                        completado = servicio.inicioEn != null
                    )
                    Hito(
                        titulo = "Check-out",
                        detalle = servicio.terminoEn?.let { selloFechaHora(it) } ?: "Pendiente",
                        completado = servicio.terminoEn != null
                    )
                    if (servicio.observaciones.isNotBlank()) {
                        Text("Observaciones: ${servicio.observaciones}", fontSize = 13.sp)
                    }
                }
            }

            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Responsable", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${titular?.nombre ?: "Sin asignar"} · ${titular?.telefono ?: ""}",
                        fontSize = 14.sp
                    )
                    TextButton(onClick = { titular?.let { onPersonaClick(it.id) } }) {
                        Text("Ver ficha del titular")
                    }
                    val acompanante = servicio.acompananteId?.let {
                        remember(estado.equipo) { vm.obtenerPersona(it) }
                    }
                    if (acompanante != null) {
                        Text("Acompañante: ${acompanante.nombre}", fontSize = 14.sp)
                    }
                }
            }

            if (servicio.inicioEn == null) {
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación de inicio (texto manual)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (servicio.terminoEn == null) {
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones del servicio (opcional)") },
                    minLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (puedeIniciar) {
                BotonPrincipal(
                    texto = "Registrar check-in",
                    icono = Icons.Default.PlayArrow,
                    cargando = enviando,
                    onClick = {
                        if (ubicacion.isBlank()) {
                            alcance.launch { snackbar.showSnackbar("Ingresa la ubicación de inicio.") }
                        } else {
                            enviando = true
                            vm.registrarCheckIn(servicio.id, ubicacion, observaciones)
                            enviando = false
                        }
                    }
                )
            } else if (servicio.estado == EstadoServicio.CONFIRMADA && servicio.inicioEn == null) {
                Text(
                    "El check-in se habilita 15 minutos antes (${servicio.inicioProgramado.minusMinutes(15).format(DateTimeFormatter.ofPattern("HH:mm"))}).",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (puedeTerminar) {
                BotonPrincipal(
                    texto = "Registrar check-out",
                    icono = Icons.Default.Check,
                    cargando = enviando,
                    onClick = {
                        enviando = true
                        vm.registrarCheckOut(servicio.id, observaciones)
                        enviando = false
                    }
                )
            }

            if (servicio.estado == EstadoServicio.COMPLETADA) {
                Text(
                    "Servicio completado. Ya está disponible en el historial.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun Hito(titulo: String, detalle: String, completado: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (completado) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (completado) Icons.Default.Check else Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (completado) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Column {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(detalle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
