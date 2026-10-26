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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.ui.components.*
import cl.apiavengers.lince.ui.esFechaHoy
import cl.apiavengers.lince.ui.fechaCorta
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun ServiciosScreen(
    vm: LinceViewModel,
    onBack: () -> Unit,
    onServicioClick: (String) -> Unit,
    onEquipoClick: () -> Unit,
    onNavigateToInicio: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {}
) {
    var filtro by remember { mutableStateOf<EstadoServicio?>(null) }
    var busqueda by remember { mutableStateOf("") }
    val usuario = vm.usuario
    val propios = if (vm.esCoordinador()) vm.servicios else vm.servicios.filter { usuario != null && it.esDe(usuario.id) }
    val filtrados = propios.filter { servicio ->
        (filtro == null || servicio.estado == filtro) &&
            (busqueda.isBlank() || servicio.titulo.contains(busqueda, true) ||
                servicio.fecha.contains(busqueda) || servicio.modalidad.etiqueta.contains(busqueda, true))
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Servicios", fontWeight = FontWeight.Bold)
                        Text(
                            if (vm.esCoordinador()) "Todos los servicios del sistema" else "Tus asignaciones",
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onEquipoClick) {
                        Icon(Icons.Default.Person, contentDescription = "Equipo")
                    }
                }
            )
        },
        bottomBar = {
            BarraInferior(
                esCoordinador = vm.esCoordinador(),
                seleccion = "Servicios",
                onInicio = onNavigateToInicio,
                onServicios = {},
                onAgenda = onNavigateToAgenda,
                onEquipo = onEquipoClick,
                onPerfil = onNavigateToPerfil
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar por fecha, estado o tipo") },
                singleLine = true,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = filtro == null, onClick = { filtro = null }, label = { Text("Todas") })
                FilterChip(
                    selected = filtro == EstadoServicio.PENDIENTE,
                    onClick = { filtro = EstadoServicio.PENDIENTE },
                    label = { Text("Pendientes") }
                )
                FilterChip(
                    selected = filtro == EstadoServicio.CONFIRMADA,
                    onClick = { filtro = EstadoServicio.CONFIRMADA },
                    label = { Text("Confirmadas") }
                )
            }
            if (filtrados.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("No hay servicios que coincidan con el filtro.", Modifier.padding(16.dp))
                }
            }
            filtrados.forEach { servicio ->
                TarjetaServicio(servicio, onClick = { onServicioClick(servicio.id) })
            }
        }
    }
}

@Composable
private fun TarjetaServicio(servicio: Servicio, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${fechaCorta(servicio.fecha)} · ${servicio.hora}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (esFechaHoy(servicio.fecha)) {
                    ChipSuave("HOY", MaterialTheme.colorScheme.secondary)
                }
            }
            Text(servicio.titulo, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(
                "${servicio.recogida} → ${servicio.destino}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChipEstadoServicio(servicio.estado)
                ChipModalidad(servicio.modalidad)
                ChipIdioma(servicio.idiomaRequerido)
            }
            if (servicio.almuerzoIncluido) {
                Text("Almuerzo incluido", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Text(
                "ID ${servicio.id}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
