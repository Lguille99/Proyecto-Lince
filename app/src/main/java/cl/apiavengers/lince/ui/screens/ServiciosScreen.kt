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
import androidx.compose.material.icons.filled.Search
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
import cl.apiavengers.lince.ui.components.*
import cl.apiavengers.lince.ui.esFechaHoy
import cl.apiavengers.lince.ui.fechaCorta
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun ServiciosScreen(
    vm: LinceViewModel,
    onBack: () -> Unit,
    onServicioClick: (String) -> Unit,
    onEquipoClick: () -> Unit,
    onSouthbound: () -> Unit = {},
    onNavigateToInicio: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {}
) {
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val t = textos()
    var filtro by remember { mutableStateOf<EstadoServicio?>(null) }
    var busqueda by remember { mutableStateOf("") }
    val usuario = estado.usuario
    val propios = if (vm.esCoordinador()) estado.servicios else estado.servicios.filter { usuario != null && it.esDe(usuario.id) }
    val filtrados = propios.filter { servicio ->
        (filtro == null || servicio.estado == filtro) &&
            (busqueda.isBlank() || servicio.titulo.contains(busqueda, true) ||
                servicio.fecha.contains(busqueda) || t.modalidad(servicio.modalidad).contains(busqueda, true))
    }
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = t.servicesTitle,
                subtitulo = if (vm.esCoordinador()) t.servicesAllSubtitle else t.servicesMineSubtitle,
                onBack = onBack,
                acciones = {
                    IconButton(onClick = onEquipoClick) {
                        Icon(Icons.Default.Person, contentDescription = t.navTeam)
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
            Button(onClick = onSouthbound, modifier = Modifier.fillMaxWidth()) {
                Text(textoSouthbound("Consultar servicios Southbound", "Browse Southbound services"))
            }
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text(t.servicesSearch) },
                singleLine = true,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(selected = filtro == null, onClick = { filtro = null }, label = { Text(t.servicesAllFilter) })
                FilterChip(
                    selected = filtro == EstadoServicio.PENDIENTE,
                    onClick = { filtro = EstadoServicio.PENDIENTE },
                    label = { Text(t.servicesPendingFilter) }
                )
                FilterChip(
                    selected = filtro == EstadoServicio.CONFIRMADA,
                    onClick = { filtro = EstadoServicio.CONFIRMADA },
                    label = { Text(t.servicesConfirmedFilter) }
                )
            }
            if (filtrados.isEmpty()) {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EstadoVacio(
                        titulo = t.servicesNoResults,
                        mensaje = t.servicesNoMatch,
                        icono = Icons.Default.Search,
                        accionTexto = t.servicesClearFilters,
                        onAccion = {
                            filtro = null
                            busqueda = ""
                        }
                    )
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
    val t = textos()
    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                    ChipSuave(
                        t.today,
                        contenedor = MaterialTheme.colorScheme.tertiaryContainer,
                        contenido = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Text(servicio.titulo, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(
                "${servicio.recogida} → ${servicio.destino}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChipEstadoServicio(servicio.estado)
                ChipModalidad(servicio.modalidad)
                ChipIdioma(servicio.idiomaRequerido)
            }
            if (servicio.almuerzoIncluido) {
                Text(t.lunchIncluded, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Text(
                "${t.id} ${servicio.id}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
