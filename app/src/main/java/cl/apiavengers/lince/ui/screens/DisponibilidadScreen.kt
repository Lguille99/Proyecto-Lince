package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.modelo.Usuario
import cl.apiavengers.lince.viewmodel.DisponibilidadViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponibilidadScreen(
    usuario: Usuario,
    onNavigateToInicio: () -> Unit,
    onNavigateToServicios: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    vm: DisponibilidadViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }
    val formato = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val esRecurso = usuario.rol != Rol.COORDINADOR
    LaunchedEffect(usuario.cuenta) {
        if (esRecurso) vm.cargar(usuario)
    }
    val opciones = listOf("Inicio", "Servicios", "Agenda", "Perfil")
    val iconos = listOf(Icons.Default.Home, Icons.AutoMirrored.Filled.List, Icons.Default.DateRange, Icons.Default.Person)

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                opciones.forEachIndexed { index, nombre ->
                    NavigationBarItem(
                        selected = index == 2,
                        enabled = !esRecurso || !state.cargando,
                        icon = { Icon(iconos[index], contentDescription = nombre) },
                        label = { Text(nombre) },
                        onClick = {
                            when (index) {
                                0 -> onNavigateToInicio()
                                1 -> onNavigateToServicios()
                                3 -> onNavigateToPerfil()
                            }
                        }
                    )
                }
            }
        }
    ) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Mi disponibilidad", style = MaterialTheme.typography.headlineMedium)
            Text("${usuario.nombre} · ${usuario.rol.etiqueta}")
            if (!esRecurso) {
                Text("La agenda personal está disponible para conductores y guías.")
            } else {
                if (state.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Día seleccionado", style = MaterialTheme.typography.titleMedium)
                        Text(state.fecha.format(formato), style = MaterialTheme.typography.headlineSmall)
                        OutlinedButton(
                            onClick = { mostrarCalendario = true },
                            enabled = state.cargado && !state.cargando,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Elegir fecha") }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Bloquear este día", modifier = Modifier.weight(1f))
                            Switch(checked = state.bloqueado, onCheckedChange = vm::cambiarBloqueo,
                                enabled = state.cargado && !state.cargando)
                        }
                        Text(if (state.bloqueado) "No disponible para esta fecha" else "Disponible para esta fecha")
                    }
                }
                Button(onClick = vm::guardar, enabled = state.cargado && !state.cargando,
                    modifier = Modifier.fillMaxWidth()) { Text("Guardar disponibilidad") }
                state.mensaje?.let { mensaje ->
                    Text(mensaje, color = if (state.esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
                if (!state.cargado && !state.cargando) {
                    OutlinedButton(onClick = { vm.cargar(usuario) }) { Text("Reintentar carga") }
                }
                if (state.cargado) {
                    Text("Días bloqueados guardados", style = MaterialTheme.typography.titleMedium)
                    val futuros = state.diasBloqueados.filter { !it.isBefore(LocalDate.now()) }.sorted()
                    if (futuros.isEmpty()) Text("No tienes días bloqueados desde hoy.")
                    futuros.forEach { fecha ->
                        TextButton(onClick = { vm.seleccionarFecha(fecha) }, enabled = !state.cargando) {
                            Text(fecha.format(formato))
                        }
                    }
                    Text("Los cambios del interruptor se aplican al pulsar Guardar. No modifican tus servicios asignados.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    if (mostrarCalendario && esRecurso) {
        val calendario = rememberDatePickerState(
            initialSelectedDateMillis = state.fecha.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isBefore(LocalDate.now())
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(enabled = calendario.selectedDateMillis != null, onClick = {
                    calendario.selectedDateMillis?.let {
                        vm.seleccionarFecha(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    mostrarCalendario = false
                }) { Text("Seleccionar") }
            },
            dismissButton = { TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") } }
        ) { DatePicker(state = calendario) }
    }
}
