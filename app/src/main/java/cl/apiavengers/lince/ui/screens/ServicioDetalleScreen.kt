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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import cl.apiavengers.lince.ui.components.*
import cl.apiavengers.lince.ui.fechaNumerica
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.ui.montoClp
import cl.apiavengers.lince.ui.selloFechaHora
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun ServicioDetalleScreen(
    vm: LinceViewModel,
    servicioId: String,
    onBack: () -> Unit,
    onPersonaClick: (String) -> Unit,
    onIniciar: (String) -> Unit,
    onNavigateToInicio: () -> Unit = {},
    onNavigateToServicios: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToEquipo: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {}
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val t = textos()
    val servicio = remember(estado.servicios, estado.historial) { vm.obtenerServicio(servicioId) }
    val snackbar = remember { SnackbarHostState() }
    var motivo by remember { mutableStateOf("") }
    var errorMotivo by remember { mutableStateOf<String?>(null) }
    var confirmarDialogo by remember { mutableStateOf(false) }
    var reasignarDialogo by remember { mutableStateOf(false) }
    var calificarDialogo by remember { mutableStateOf(false) }
    var puntaje by remember { mutableStateOf(0) }

    val mensaje = estado.mensaje
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            if (!estado.esError) {
                motivo = ""
                errorMotivo = null
                confirmarDialogo = false
                reasignarDialogo = false
                calificarDialogo = false
            }
            snackbar.showSnackbar(mensaje)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            BarraSuperior(titulo = t.detailTitle, onBack = onBack)
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
                Text(t.serviceNotFound)
            }
            return@Scaffold
        }
        val titular = remember(estado.equipo) { vm.obtenerPersona(servicio.asignadoA) }
        val acompanante = remember(estado.equipo, servicio.acompananteId) {
            servicio.acompananteId?.let { vm.obtenerPersona(it) }
        }

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
                Text("${t.id} ${servicio.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(servicio.titulo, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChipSuave(
                    servicio.tipo,
                    contenedor = MaterialTheme.colorScheme.primaryContainer,
                    contenido = MaterialTheme.colorScheme.onPrimaryContainer
                )
                ChipModalidad(servicio.modalidad)
                ChipIdioma(servicio.idiomaRequerido)
            }

            Seccion(t.serviceInfo) {
                FilaDato(t.date, fechaNumerica(servicio.fecha))
                FilaDato(t.time, servicio.hora)
                FilaDato(t.pickupPoint, servicio.recogida)
                FilaDato(t.destination, servicio.destino)
                if (servicio.hotelRecogida.isNotBlank() && servicio.hotelRecogida != servicio.recogida) {
                    FilaDato(t.pickupHotel, servicio.hotelRecogida)
                }
                FilaDato(t.passengers, "${servicio.pasajeros}")
                FilaDato(t.tourDays, "${servicio.diasTour}")
                FilaDato(t.lunch, if (servicio.almuerzoIncluido) t.included else t.notIncluded)
                FilaDato(t.fare, montoClp(servicio.tarifa))
            }

            if (servicio.resenaHistorica.isNotBlank()) {
                Seccion(t.forYourTrip) {
                    Text(servicio.resenaHistorica, fontSize = 14.sp)
                }
            }

            if (servicio.itinerario.isNotEmpty()) {
                Seccion(t.itinerary) {
                    servicio.itinerario.forEachIndexed { index, paso ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                            Text(paso, fontSize = 14.sp)
                        }
                    }
                }
            }

            Seccion(t.assignedTeam) {
                FilaPersona(
                    rol = t.holder,
                    persona = titular,
                    onClick = { titular?.let { onPersonaClick(it.id) } }
                )
                if (acompanante != null) {
                    HorizontalDivider()
                    FilaPersona(
                        rol = t.companionRole,
                        persona = acompanante,
                        onClick = { onPersonaClick(acompanante.id) }
                    )
                }
            }

            if (servicio.respondidoPor != null || servicio.inicioEn != null || servicio.reasignadoPor != null) {
                Seccion(t.traceability) {
                    val autor = servicio.respondidoPor?.let { remember(estado.equipo) { vm.obtenerPersona(it) } }
                    if (servicio.respondidoEn != null) {
                        FilaDato(
                            if (servicio.estado == EstadoServicio.RECHAZADA) t.rejected else t.confirmed,
                            "${selloFechaHora(servicio.respondidoEn)} · ${autor?.nombre ?: servicio.respondidoPor.orEmpty()}"
                        )
                    }
                    if (!servicio.motivoRechazo.isNullOrBlank()) {
                        Text(
                            t.reason(servicio.motivoRechazo.orEmpty()),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    servicio.inicioEn?.let {
                        FilaDato("Check-in", "${selloFechaHora(it)} · ${servicio.ubicacionInicio.orEmpty()}")
                    }
                    servicio.terminoEn?.let { FilaDato("Check-out", selloFechaHora(it)) }
                    servicio.reasignadoEn?.let {
                        val quien = servicio.reasignadoPor?.let { id -> remember(estado.equipo) { vm.obtenerPersona(id) } }
                        FilaDato(t.reassigned, "${selloFechaHora(it)} · ${quien?.nombre ?: t.coordination}")
                    }
                    servicio.puntaje?.let {
                        FilaDato(t.ratingLabel, "$it/5 · ${selloFechaHora(servicio.calificadoEn)}")
                    }
                }
            }

            if (vm.puedeResponder(servicio)) {
                Text(t.respondAssignment, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = motivo,
                    onValueChange = {
                        motivo = it
                        if (errorMotivo != null) errorMotivo = null
                    },
                    label = { Text(t.observationLabel) },
                    minLines = 2,
                    isError = errorMotivo != null,
                    supportingText = { errorMotivo?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                BotonPrincipal(
                    texto = t.confirmAssignment,
                    icono = Icons.Default.Check,
                    onClick = { confirmarDialogo = true }
                )
                BotonPeligro(
                    texto = t.rejectAssignment,
                    icono = Icons.Default.Close,
                    onClick = {
                        if (motivo.isBlank()) {
                            errorMotivo = t.rejectReasonRequired
                        } else {
                            vm.rechazar(servicio.id, motivo)
                        }
                    }
                )
            }

            if (vm.puedeIniciar(servicio)) {
                BotonPrincipal(
                    texto = t.startService,
                    icono = Icons.Default.PlayArrow,
                    onClick = { onIniciar(servicio.id) }
                )
            }

            if (vm.esCoordinador() && (servicio.estado == EstadoServicio.PENDIENTE || servicio.estado == EstadoServicio.RECHAZADA)) {
                BotonSecundario(texto = t.reassignService, onClick = { reasignarDialogo = true })
            }

            if (vm.esCoordinador() && servicio.estado == EstadoServicio.COMPLETADA && servicio.puntaje == null) {
                BotonSecundario(texto = t.rateService, onClick = { calificarDialogo = true })
            }

            Text(
                t.traceabilityNote,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (confirmarDialogo && servicio != null) {
        AlertDialog(
            onDismissRequest = { confirmarDialogo = false },
            title = { Text(t.confirmAssignment) },
            text = { Text(t.confirmAssignmentText(servicio.id)) },
            confirmButton = {
                TextButton(onClick = { vm.confirmar(servicio.id) }) { Text(t.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarDialogo = false }) { Text(t.cancel) }
            }
        )
    }

    if (reasignarDialogo && servicio != null) {
        val candidatos = remember(servicio.id, estado.equipo) { vm.candidatosReasignacion(servicio.id) }
        var seleccionado by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { reasignarDialogo = false },
            title = { Text(t.reassignService) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (candidatos.isEmpty()) {
                        Text(t.noCandidates)
                    } else {
                        Text(t.selectNewHolder)
                        candidatos.forEach { persona ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                        .clickable { seleccionado = persona.id }
                                        .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = seleccionado == persona.id,
                                    onClick = { seleccionado = persona.id }
                                )
                                Text("${persona.nombre} (${persona.id})")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        seleccionado?.let { vm.reasignar(servicio.id, it) }
                    },
                    enabled = seleccionado != null
                ) { Text(t.reassign) }
            },
            dismissButton = {
                TextButton(onClick = { reasignarDialogo = false }) { Text(t.cancel) }
            }
        )
    }

    if (calificarDialogo && servicio != null) {
        AlertDialog(
            onDismissRequest = { calificarDialogo = false },
            title = { Text(t.rateDialogTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t.rateQuestion)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { valor ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = t.stars(valor),
                                tint = if (valor <= puntaje) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(32.dp).clickable { puntaje = valor }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.calificar(servicio.id, puntaje) },
                    enabled = puntaje in 1..5
                ) { Text(t.save) }
            },
            dismissButton = {
                TextButton(onClick = { calificarDialogo = false }) { Text(t.cancel) }
            }
        )
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FilaPersona(rol: String, persona: Usuario?, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
            Text(rol, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(persona?.nombre ?: textos().unassigned, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        if (persona != null) {
            Text(persona.telefono, fontSize = 12.sp)
        }
    }
}
