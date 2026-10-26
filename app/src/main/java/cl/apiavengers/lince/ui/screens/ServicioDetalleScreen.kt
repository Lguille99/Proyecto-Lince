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
    val servicio = remember(vm.servicios, vm.historial) { vm.obtenerServicio(servicioId) }
    val snackbar = remember { SnackbarHostState() }
    var motivo by remember { mutableStateOf("") }
    var errorMotivo by remember { mutableStateOf<String?>(null) }
    var confirmarDialogo by remember { mutableStateOf(false) }
    var reasignarDialogo by remember { mutableStateOf(false) }
    var calificarDialogo by remember { mutableStateOf(false) }
    var puntaje by remember { mutableStateOf(0) }

    val mensaje = vm.mensaje
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbar.showSnackbar(mensaje)
            vm.limpiarMensaje()
            if (!vm.esError) {
                motivo = ""
                errorMotivo = null
                confirmarDialogo = false
                reasignarDialogo = false
                calificarDialogo = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del servicio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
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
        val titular = remember(vm.equipo) { vm.obtenerPersona(servicio.asignadoA) }
        val acompanante = remember(vm.equipo, servicio.acompananteId) {
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
                Text("ID ${servicio.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Text(servicio.titulo, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChipSuave("${servicio.tipo}", MaterialTheme.colorScheme.primary)
                ChipModalidad(servicio.modalidad)
                ChipIdioma(servicio.idiomaRequerido)
            }

            Seccion("Información del servicio") {
                FilaDato("Fecha", fechaNumerica(servicio.fecha))
                FilaDato("Hora", servicio.hora)
                FilaDato("Punto de salida", servicio.recogida)
                FilaDato("Destino", servicio.destino)
                if (servicio.hotelRecogida.isNotBlank() && servicio.hotelRecogida != servicio.recogida) {
                    FilaDato("Hotel de recogida", servicio.hotelRecogida)
                }
                FilaDato("Pasajeros", "${servicio.pasajeros}")
                FilaDato("Días de tour", "${servicio.diasTour}")
                FilaDato("Almuerzo", if (servicio.almuerzoIncluido) "Incluido" else "No incluido")
                FilaDato("Tarifa", montoClp(servicio.tarifa))
            }

            if (servicio.resenaHistorica.isNotBlank()) {
                Seccion("Para tu recorrido") {
                    Text(servicio.resenaHistorica, fontSize = 14.sp)
                }
            }

            if (servicio.itinerario.isNotEmpty()) {
                Seccion("Itinerario") {
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

            Seccion("Equipo asignado") {
                FilaPersona(
                    rol = "Titular",
                    persona = titular,
                    onClick = { titular?.let { onPersonaClick(it.id) } }
                )
                if (acompanante != null) {
                    HorizontalDivider()
                    FilaPersona(
                        rol = "Acompañante",
                        persona = acompanante,
                        onClick = { onPersonaClick(acompanante.id) }
                    )
                }
            }

            if (servicio.respondidoPor != null || servicio.inicioEn != null || servicio.reasignadoPor != null) {
                Seccion("Trazabilidad") {
                    val autor = servicio.respondidoPor?.let { remember(vm.equipo) { vm.obtenerPersona(it) } }
                    if (servicio.respondidoEn != null) {
                        FilaDato(
                            if (servicio.estado == EstadoServicio.RECHAZADA) "Rechazada" else "Confirmada",
                            "${selloFechaHora(servicio.respondidoEn)} · ${autor?.nombre ?: servicio.respondidoPor.orEmpty()}"
                        )
                    }
                    if (!servicio.motivoRechazo.isNullOrBlank()) {
                        Text(
                            "Motivo: ${servicio.motivoRechazo}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    servicio.inicioEn?.let {
                        FilaDato("Check-in", "${selloFechaHora(it)} · ${servicio.ubicacionInicio.orEmpty()}")
                    }
                    servicio.terminoEn?.let { FilaDato("Check-out", selloFechaHora(it)) }
                    servicio.reasignadoEn?.let {
                        val quien = servicio.reasignadoPor?.let { id -> remember(vm.equipo) { vm.obtenerPersona(id) } }
                        FilaDato("Reasignado", "${selloFechaHora(it)} · ${quien?.nombre ?: "Coordinación"}")
                    }
                    servicio.puntaje?.let {
                        FilaDato("Calificación", "$it/5 · ${selloFechaHora(servicio.calificadoEn)}")
                    }
                }
            }

            if (vm.puedeResponder(servicio)) {
                Text("Responde la asignación", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = motivo,
                    onValueChange = {
                        motivo = it
                        if (errorMotivo != null) errorMotivo = null
                    },
                    label = { Text("Observación o motivo de rechazo (opcional)") },
                    minLines = 2,
                    isError = errorMotivo != null,
                    supportingText = { errorMotivo?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                BotonPrincipal(
                    texto = "Confirmar asignación",
                    icono = Icons.Default.Check,
                    onClick = { confirmarDialogo = true }
                )
                BotonPeligro(
                    texto = "Rechazar asignación",
                    icono = Icons.Default.Close,
                    onClick = {
                        if (motivo.isBlank()) {
                            errorMotivo = "Para rechazar, indica el motivo."
                        } else {
                            vm.rechazar(servicio.id, motivo)
                        }
                    }
                )
            }

            if (vm.puedeIniciar(servicio)) {
                BotonPrincipal(
                    texto = "Iniciar servicio (check-in)",
                    icono = Icons.Default.PlayArrow,
                    onClick = { onIniciar(servicio.id) }
                )
            }

            if (vm.esCoordinador() && (servicio.estado == EstadoServicio.PENDIENTE || servicio.estado == EstadoServicio.RECHAZADA)) {
                BotonSecundario(texto = "Reasignar servicio", onClick = { reasignarDialogo = true })
            }

            if (vm.esCoordinador() && servicio.estado == EstadoServicio.COMPLETADA && servicio.puntaje == null) {
                BotonSecundario(texto = "Calificar servicio", onClick = { calificarDialogo = true })
            }

            Text(
                "Cada respuesta queda registrada con autor y fecha para mantener la trazabilidad.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }

    if (confirmarDialogo && servicio != null) {
        AlertDialog(
            onDismissRequest = { confirmarDialogo = false },
            title = { Text("Confirmar asignación") },
            text = { Text("Se notificará al coordinador que aceptaste el servicio ${servicio.id}.") },
            confirmButton = {
                TextButton(onClick = { vm.confirmar(servicio.id) }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarDialogo = false }) { Text("Cancelar") }
            }
        )
    }

    if (reasignarDialogo && servicio != null) {
        val candidatos = remember(servicio.id, vm.equipo) { vm.candidatosReasignacion(servicio.id) }
        var seleccionado by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { reasignarDialogo = false },
            title = { Text("Reasignar servicio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (candidatos.isEmpty()) {
                        Text("No hay candidatos disponibles con el mismo rol e idioma.")
                    } else {
                        Text("Selecciona al nuevo titular:")
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
                ) { Text("Reasignar") }
            },
            dismissButton = {
                TextButton(onClick = { reasignarDialogo = false }) { Text("Cancelar") }
            }
        )
    }

    if (calificarDialogo && servicio != null) {
        AlertDialog(
            onDismissRequest = { calificarDialogo = false },
            title = { Text("Calificar servicio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Cómo fue el desempeño del titular?")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { valor ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "$valor estrellas",
                                tint = if (valor <= puntaje) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
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
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { calificarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
        Text(etiqueta, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
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
            Text(rol, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(persona?.nombre ?: "Sin asignar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        if (persona != null) {
            Text(persona.telefono, fontSize = 12.sp)
        }
    }
}
