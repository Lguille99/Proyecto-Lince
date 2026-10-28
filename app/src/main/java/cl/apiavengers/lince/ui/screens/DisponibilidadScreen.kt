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
import cl.apiavengers.lince.datos.SesionSupabase
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import cl.apiavengers.lince.ui.components.BarraInferior
import cl.apiavengers.lince.ui.components.BarraSuperior
import cl.apiavengers.lince.ui.components.BotonPrincipal
import cl.apiavengers.lince.ui.fechaLarga
import cl.apiavengers.lince.viewmodel.DisponibilidadViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val state by vm.uiState.collectAsState()
    val esRemoto = SesionSupabase.activa
    var mes by remember { mutableStateOf(YearMonth.now()) }
    val hoy = LocalDate.now()
    val nombresMes = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "CL"))

    Scaffold(
        topBar = {
            BarraSuperior(titulo = "Mi disponibilidad", onBack = onBack)
        },
        bottomBar = {
            BarraInferior(
                esCoordinador = false,
                seleccion = "Agenda",
                onInicio = onNavigateToInicio,
                onServicios = onNavigateToServicios,
                onAgenda = {},
                onPerfil = onNavigateToPerfil
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
                "Marca los días en que no podrás trabajar. Los bloqueos requieren 7 días de anticipación.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
                        }
                        Text(
                            mes.format(nombresMes)
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "CL")) else it.toString() },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { mes = mes.plusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { dia ->
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

            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Día seleccionado", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(fechaLarga(state.fecha.toString()), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bloquear este día", fontSize = 14.sp)
                        Switch(
                            checked = state.bloqueado,
                            onCheckedChange = { vm.cambiarBloqueo(it) },
                            enabled = state.cargado && !state.cargando
                        )
                    }
                    if (state.bloqueado) {
                        Text("No recibirás asignaciones para este día.", fontSize = 12.sp)
                    }
                }
            }

            BotonPrincipal(
                texto = "Guardar disponibilidad",
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
                if (esRemoto) {
                    "Los cambios se guardan en Supabase. No puedes bloquear un día con un servicio asignado."
                } else {
                    "Los cambios se guardan en este dispositivo. No puedes bloquear un día con un servicio asignado."
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
