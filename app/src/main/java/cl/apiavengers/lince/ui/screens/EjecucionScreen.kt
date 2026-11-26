@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.apiavengers.lince.datos.UbicacionGps
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Ubicacion
import cl.apiavengers.lince.modelo.evaluarLlegada
import cl.apiavengers.lince.ui.components.*
import cl.apiavengers.lince.ui.components.MapaUbicacion
import cl.apiavengers.lince.ui.components.MarcaMapa
import cl.apiavengers.lince.ui.fechaNumerica
import cl.apiavengers.lince.ui.horaMinuto
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.ui.selloFechaHora
import cl.apiavengers.lince.ui.theme.Colores
import cl.apiavengers.lince.viewmodel.LinceViewModel
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val t = textos()
    val servicio = remember(estado.servicios, estado.historial) { vm.obtenerServicio(servicioId) }
    val snackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    val contexto = LocalContext.current
    val gps = remember(contexto) { UbicacionGps(contexto) }
    var ubicacion by remember(servicio?.id) {
        mutableStateOf(servicio?.hotelRecogida?.ifBlank { servicio.recogida }.orEmpty())
    }
    var ubicacionTermino by remember(servicio?.id) {
        mutableStateOf(servicio?.destino.orEmpty())
    }
    var observaciones by remember { mutableStateOf("") }
    var ahora by remember { mutableStateOf(LocalDateTime.now()) }
    var enviando by remember { mutableStateOf(false) }
    var ubicacionActual by remember { mutableStateOf<Ubicacion?>(null) }
    var cargandoUbicacion by remember { mutableStateOf(false) }
    var errorUbicacion by remember { mutableStateOf<String?>(null) }

    val obtenerUbicacion: suspend () -> Unit = {
        cargandoUbicacion = true
        errorUbicacion = null
        try {
            ubicacionActual = gps.obtenerUbicacion()
        } catch (_: SecurityException) {
            errorUbicacion = t.locationPermissionDenied
        } catch (problema: Exception) {
            errorUbicacion = t.traducir(problema.message) ?: t.locationUnavailable
        } finally {
            cargandoUbicacion = false
        }
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos.values.any { it } || gps.tienePermiso()) {
            alcance.launch { obtenerUbicacion() }
        } else {
            errorUbicacion = t.locationPermissionDenied
        }
    }

    val actualizarUbicacion: () -> Unit = {
        if (gps.tienePermiso()) {
            alcance.launch { obtenerUbicacion() }
        } else {
            lanzadorPermiso.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(servicio?.id) {
        val actual = servicio
        if (actual != null &&
            actual.estado == EstadoServicio.CONFIRMADA &&
            actual.terminoEn == null &&
            gps.tienePermiso()
        ) {
            obtenerUbicacion()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            ahora = LocalDateTime.now()
        }
    }
    LaunchedEffect(estado.cargando) {
        if (!estado.cargando) enviando = false
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
            LinceTopBar(titulo = t.executionTitle, navegarAtras = onBack)
        },
        bottomBar = {
            val esCoordinador = vm.esCoordinador()
            LinceFloatingBottomBar(
                items = listOf(
                    Icons.Default.Home to t.navHome,
                    Icons.AutoMirrored.Filled.List to t.navServices,
                    if (esCoordinador) Icons.Default.Person to t.navTeam
                    else Icons.Default.DateRange to t.navAgenda,
                    Icons.Default.Face to t.navProfile
                ),
                indiceSeleccionado = 1,
                enSeleccionar = { indice ->
                    when (indice) {
                        0 -> onNavigateToInicio()
                        1 -> onNavigateToServicios()
                        2 -> if (esCoordinador) onNavigateToEquipo() else onNavigateToAgenda()
                        3 -> onNavigateToPerfil()
                    }
                },
                modifier = Modifier.navigationBarsPadding()
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
        val inicioPermitido = servicio.inicioProgramado.minusMinutes(15)
        val evaluacion = remember(servicio, ubicacionActual, ahora) {
            ubicacionActual?.let { evaluarLlegada(servicio, it, ahora) }
        }
        val puedeIniciar = servicio.estado == EstadoServicio.CONFIRMADA &&
            servicio.inicioEn == null &&
            !ahora.isBefore(inicioPermitido) &&
            evaluacion?.dentro == true
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
                Text("${t.id} ${servicio.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            LinceCard(
                fondo = Colores.superficieNeutra,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(t.executionMilestones, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Hito(
                        titulo = "Check-in",
                        detalle = servicio.inicioEn?.let {
                            "${selloFechaHora(it)} · ${servicio.ubicacionInicio.orEmpty()}"
                        } ?: t.pending,
                        completado = servicio.inicioEn != null
                    )
                    Hito(
                        titulo = "Check-out",
                        detalle = servicio.terminoEn?.let {
                            "${selloFechaHora(it)} · ${servicio.ubicacionTermino.orEmpty()}"
                        } ?: t.pending,
                        completado = servicio.terminoEn != null
                    )
                    if (servicio.observaciones.isNotBlank()) {
                        Text(t.observations(servicio.observaciones), fontSize = 13.sp)
                    }
                }
            }

            LinceCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t.responsibleTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${titular?.nombre ?: t.unassigned} · ${titular?.telefono ?: ""}",
                        fontSize = 14.sp
                    )
                    TextButton(onClick = { titular?.let { onPersonaClick(it.id) } }) {
                        Text(t.viewHolderProfile)
                    }
                    val acompanante = servicio.acompananteId?.let {
                        remember(estado.equipo) { vm.obtenerPersona(it) }
                    }
                    if (acompanante != null) {
                        Text(t.companion(acompanante.nombre), fontSize = 14.sp)
                    }
                }
            }

            TarjetaUbicacion(
                servicio = servicio,
                ubicacion = ubicacionActual,
                evaluacion = evaluacion,
                cargando = cargandoUbicacion,
                error = errorUbicacion,
                tienePermiso = gps.tienePermiso(),
                onActualizar = actualizarUbicacion
            )

            LinceCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t.mapTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    val marcadores = remember(servicio, ubicacionActual, t) {
                        buildList {
                            if (servicio.tieneGeocerca) {
                                add(MarcaMapa(servicio.latitudRecogida!!, servicio.longitudRecogida!!, t.mapPickup))
                            }
                            ubicacionActual?.let {
                                add(MarcaMapa(it.latitud, it.longitud, t.mapCurrentLocation))
                            }
                            if (servicio.ultimaLatitud != null && servicio.ultimaLongitud != null) {
                                add(MarcaMapa(servicio.ultimaLatitud!!, servicio.ultimaLongitud!!, t.lastPositionTitle))
                            }
                        }
                    }
                    MapaUbicacion(
                        centroLatitud = servicio.latitudRecogida,
                        centroLongitud = servicio.longitudRecogida,
                        radioMetros = servicio.radioGeocercaMetros,
                        marcadores = marcadores
                    )
                }
            }

            if (servicio.inicioEn == null) {
                LinceInputField(
                    etiqueta = t.startLocation,
                    valor = ubicacion,
                    enCambioValor = { ubicacion = it }
                )
            }
            if (servicio.terminoEn == null) {
                LinceInputField(
                    etiqueta = t.serviceNotes,
                    valor = observaciones,
                    enCambioValor = { observaciones = it },
                    singleLine = false,
                    minLines = 2
                )
            }
            if (puedeTerminar) {
                LinceInputField(
                    etiqueta = t.dropoffLocation,
                    valor = ubicacionTermino,
                    enCambioValor = { ubicacionTermino = it }
                )
            }

            if (puedeIniciar) {
                BotonPrincipal(
                    texto = t.registerCheckin,
                    icono = Icons.Default.PlayArrow,
                    cargando = enviando,
                    onClick = {
                        val posicion = ubicacionActual
                        val revision = posicion?.let { evaluarLlegada(servicio, it, LocalDateTime.now()) }
                        when {
                            posicion == null -> {
                                alcance.launch { snackbar.showSnackbar(t.locationRequired) }
                                actualizarUbicacion()
                            }
                            revision == null -> {
                                alcance.launch { snackbar.showSnackbar(t.locationNoGeofence) }
                            }
                            !revision.dentro -> {
                                alcance.launch {
                                    snackbar.showSnackbar(
                                        t.locationOutsideValue(
                                            revision.distanciaMetros.roundToInt(),
                                            revision.radioMetros
                                        )
                                    )
                                }
                            }
                            else -> {
                                enviando = true
                                vm.registrarCheckIn(
                                    servicioId = servicio.id,
                                    ubicacion = ubicacion.ifBlank { servicio.hotelRecogida },
                                    observaciones = observaciones,
                                    posicion = posicion
                                )
                            }
                        }
                    }
                )
            } else if (servicio.estado == EstadoServicio.CONFIRMADA && servicio.inicioEn == null) {
                Text(
                    if (!ahora.isBefore(inicioPermitido)) t.locationRequired else
                        t.checkinEnabled(horaMinuto(servicio.inicioProgramado.minusMinutes(15))),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (puedeTerminar) {
                val ultimaLatitud = servicio.ultimaLatitud
                val ultimaLongitud = servicio.ultimaLongitud
                if (ultimaLatitud != null && ultimaLongitud != null) {
                    LinceCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(t.lastPositionTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                t.lastPositionValue(
                                    formatoCoordenada(ultimaLatitud),
                                    formatoCoordenada(ultimaLongitud),
                                    servicio.ultimaPrecisionMetros?.roundToInt()
                                        ?.let(t::locationAccuracyValue) ?: "—",
                                    selloFechaHora(servicio.ultimaUbicacionEn)
                                ),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                BotonSecundario(
                    texto = t.reportLocation,
                    cargando = enviando,
                    onClick = {
                        val posicion = ubicacionActual
                        if (posicion == null) {
                            actualizarUbicacion()
                        } else {
                            enviando = true
                            vm.reportarUbicacion(servicio.id, posicion)
                        }
                    }
                )
                BotonPrincipal(
                    texto = t.checkoutWithLocation,
                    icono = Icons.Default.Check,
                    cargando = enviando,
                    onClick = {
                        val posicion = ubicacionActual
                        if (posicion == null) {
                            alcance.launch { snackbar.showSnackbar(t.locationRequired) }
                            actualizarUbicacion()
                        } else {
                            enviando = true
                            vm.registrarCheckOut(
                                servicioId = servicio.id,
                                observaciones = observaciones,
                                posicion = posicion,
                                ubicacionTermino = ubicacionTermino.ifBlank { servicio.destino }
                            )
                        }
                    }
                )
            }

            if (servicio.estado == EstadoServicio.COMPLETADA) {
                Text(
                    t.serviceCompleted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun TarjetaUbicacion(
    servicio: cl.apiavengers.lince.modelo.Servicio,
    ubicacion: Ubicacion?,
    evaluacion: cl.apiavengers.lince.modelo.EvaluacionGeocerca?,
    cargando: Boolean,
    error: String?,
    tienePermiso: Boolean,
    onActualizar: () -> Unit
) {
    val t = textos()
    LinceCard(
        fondo = Colores.superficieNeutra,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(t.locationTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${t.geofenceRadius}: ${t.geofenceRadiusValue(servicio.radioGeocercaMetros)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                servicio.hotelRecogida.ifBlank { servicio.recogida },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!servicio.tieneGeocerca) {
                Text(t.locationNoGeofence, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
            } else if (cargando) {
                Text(t.locationGetting, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (ubicacion != null) {
                Text(t.locationGps, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    t.locationCoordinates(
                        formatoCoordenada(ubicacion.latitud),
                        formatoCoordenada(ubicacion.longitud)
                    ),
                    fontSize = 13.sp
                )
                Text(
                    "${t.locationAccuracy}: ${ubicacion.precisionMetros?.roundToInt()?.let(t::locationAccuracyValue) ?: "—"}",
                    fontSize = 13.sp
                )
                if (ubicacion.obtenidaEn != null) {
                    Text(
                        "${t.locationTime}: ${selloFechaHora(ubicacion.obtenidaEn)}",
                        fontSize = 13.sp
                    )
                }
                if (evaluacion != null) {
                    Text(
                        "${t.locationDistance}: ${t.locationDistanceValue(evaluacion.distanciaMetros.roundToInt())}",
                        fontSize = 13.sp
                    )
                    Text(
                        if (evaluacion.dentro) {
                            t.locationInsideValue(
                                evaluacion.distanciaMetros.roundToInt(),
                                evaluacion.radioMetros
                            )
                        } else {
                            t.locationOutsideValue(
                                evaluacion.distanciaMetros.roundToInt(),
                                evaluacion.radioMetros
                            )
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (evaluacion.dentro) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.error
                    )
                    if (servicio.inicioEn == null) {
                        if (evaluacion.tardanza) {
                            Text(
                                t.arrivalLateValue(abs(evaluacion.minutosAntes).toInt()),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                t.arrivalOnTimeValue(evaluacion.minutosAntes.toInt().coerceAtLeast(0)),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    if (evaluacion.precisionBaja) {
                        Text(
                            "${t.locationAccuracy}: " +
                                (evaluacion.precisionMetros?.roundToInt()
                                    ?.let(t::locationAccuracyValue) ?: ""),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else if (error != null) {
                Text(error, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
            } else {
                Text(t.locationUnavailable, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            BotonSecundario(
                texto = if (tienePermiso) t.locationRefresh else t.locationRequestPermission,
                onClick = onActualizar
            )
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

private fun formatoCoordenada(valor: Double): String =
    String.format(Locale.US, "%.5f", valor)
