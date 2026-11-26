@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.apiavengers.lince.datos.SesionSupabase
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import cl.apiavengers.lince.ui.components.BotonPrincipal
import cl.apiavengers.lince.ui.components.LinceCard
import cl.apiavengers.lince.ui.components.LinceFloatingBottomBar
import cl.apiavengers.lince.ui.components.LinceTopBar
import cl.apiavengers.lince.ui.fechaLarga
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.ui.mesLargo
import cl.apiavengers.lince.ui.theme.Colores
import cl.apiavengers.lince.viewmodel.DisponibilidadViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

@Composable
fun DisponibilidadScreen(
    vm: DisponibilidadViewModel,
    usuario: Usuario?,
    serviciosAsignados: List<Servicio> = emptyList(),
    onBack: () -> Unit,
    onCerrarSesion: () -> Unit = {},
    onNavigateToInicio: () -> Unit = {},
    onNavigateToServicios: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {}
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val t = textos()
    val esRemoto = SesionSupabase.activa
    var mes by remember { mutableStateOf(YearMonth.now()) }
    var mostrarSelector by remember { mutableStateOf(false) }
    val hoy = remember { LocalDate.now() }
    val hoyMillis = remember(hoy) { hoy.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
    val fechasSeleccionables = remember(hoyMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= hoyMillis
            override fun isSelectableYear(year: Int): Boolean = year >= hoy.year
        }
    }
    Scaffold(
        topBar = {
            LinceTopBar(titulo = t.availabilityTitle, navegarAtras = onBack)
        },
        bottomBar = {
            LinceFloatingBottomBar(
                items = listOf(
                    Icons.Default.Home to t.navHome,
                    Icons.AutoMirrored.Filled.List to t.navServices,
                    Icons.Default.DateRange to t.navAgenda,
                    Icons.Default.Person to t.navProfile
                ),
                indiceSeleccionado = 2,
                enSeleccionar = { indice ->
                    when (indice) {
                        0 -> onNavigateToInicio()
                        1 -> onNavigateToServicios()
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                t.availabilityIntro,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinceCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { mes = mes.minusMonths(1) },
                            enabled = mes.isAfter(YearMonth.from(hoy))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = t.previousMonth)
                        }
                        Text(
                            mesLargo(mes),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { mes = mes.plusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = t.nextMonth)
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        t.weekdays.forEach { dia ->
                            Text(
                                dia,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    val primerDia = mes.atDay(1)
                    val desplazamiento = primerDia.dayOfWeek.value - 1
                    val totalCeldas = desplazamiento + mes.lengthOfMonth()
                    val filas = (totalCeldas + 6) / 7
                    repeat(filas) { fila ->
                        Row(Modifier.fillMaxWidth()) {
                            repeat(7) { columna ->
                                val dia = fila * 7 + columna - desplazamiento + 1
                                Box(Modifier.weight(1f).aspectRatio(1.1f), contentAlignment = Alignment.Center) {
                                    if (dia in 1..mes.lengthOfMonth()) {
                                        val fecha = mes.atDay(dia)
                                        val seleccionado = fecha == state.fecha
                                        val bloqueado = fecha in state.diasBloqueados
                                        val pasado = fecha.isBefore(hoy)
                                        Surface(
                                            shape = CircleShape,
                                            color = when {
                                                seleccionado -> MaterialTheme.colorScheme.primary
                                                bloqueado -> MaterialTheme.colorScheme.errorContainer
                                                else -> Color.Transparent
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable(enabled = !pasado) { vm.seleccionarFecha(fecha) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    dia.toString(),
                                                    fontSize = 14.sp,
                                                    fontWeight = if (seleccionado || fecha == hoy) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        seleccionado -> MaterialTheme.colorScheme.onPrimary
                                                        pasado -> MaterialTheme.colorScheme.onSurfaceVariant
                                                        bloqueado -> MaterialTheme.colorScheme.onErrorContainer
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LinceCard(
                fondo = Colores.superficieNeutra,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t.selectedDay, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(fechaLarga(state.fecha.toString()), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { mostrarSelector = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(t.chooseAnotherDate)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(t.blockDay, fontSize = 14.sp)
                        Switch(
                            checked = state.bloqueado,
                            onCheckedChange = { vm.cambiarBloqueo(it) },
                            enabled = state.cargado && !state.cargando
                        )
                    }
                    if (state.bloqueado) {
                        Text(t.noAssignmentsThatDay, fontSize = 12.sp)
                    }
                }
            }

            BotonPrincipal(
                texto = t.saveAvailability,
                onClick = { vm.guardar(serviciosAsignados) },
                enabled = state.cargado,
                cargando = state.cargando
            )
            state.mensaje?.let { mensaje ->
                Text(
                    mensaje,
                    fontSize = 13.sp,
                    color = if (state.esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                if (esRemoto) t.changesRemote else t.changesLocal,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (mostrarSelector) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = fechasSeleccionables
        )
        DatePickerDialog(
            onDismissRequest = { mostrarSelector = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val fecha = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            mes = YearMonth.from(fecha)
                            vm.seleccionarFecha(fecha)
                        }
                        mostrarSelector = false
                    }
                ) { Text(t.accept) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelector = false }) { Text(t.cancel) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
