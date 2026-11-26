@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.ui.components.ChipEstadoServicio
import cl.apiavengers.lince.ui.components.ChipIdioma
import cl.apiavengers.lince.ui.components.ChipModalidad
import cl.apiavengers.lince.ui.components.ChipSuave
import cl.apiavengers.lince.ui.components.EstadoVacio
import cl.apiavengers.lince.ui.components.LinceCard
import cl.apiavengers.lince.ui.components.LinceFilterChipRow
import cl.apiavengers.lince.ui.components.LinceFloatingBottomBar
import cl.apiavengers.lince.ui.components.LinceSearchBar
import cl.apiavengers.lince.ui.components.LinceTopBar
import cl.apiavengers.lince.ui.esFechaHoy
import cl.apiavengers.lince.ui.fechaCorta
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.ui.theme.Colores
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
    val esCoord = vm.esCoordinador()
    val propios = remember(estado.servicios, usuario?.id, esCoord) {
        if (esCoord) estado.servicios else estado.servicios.filter { usuario != null && it.esDe(usuario.id) }
    }
    val filtrados = remember(propios, filtro, busqueda) {
        propios.filter { servicio ->
            (filtro == null || servicio.estado == filtro) &&
                (busqueda.isBlank() || servicio.titulo.contains(busqueda, true) ||
                    servicio.fecha.contains(busqueda) || t.modalidad(servicio.modalidad).contains(busqueda, true))
        }
    }
    Scaffold(
        topBar = {
            LinceTopBar(
                titulo = t.servicesTitle,
                subtitulo = if (esCoord) t.servicesAllSubtitle else t.servicesMineSubtitle,
                navegarAtras = onBack,
                accionDerecha = onEquipoClick,
                iconoDerecha = Icons.Default.Person
            )
        },
        bottomBar = {
            val indice = 1
            val items = if (esCoord) {
                listOf(
                    Icons.Default.Home to t.navHome,
                    Icons.AutoMirrored.Filled.List to t.navServices,
                    Icons.Default.Person to t.navTeam,
                    Icons.Default.Face to t.navProfile
                )
            } else {
                listOf(
                    Icons.Default.Home to t.navHome,
                    Icons.AutoMirrored.Filled.List to t.navServices,
                    Icons.Default.DateRange to t.navAgenda,
                    Icons.Default.Face to t.navProfile
                )
            }
            LinceFloatingBottomBar(
                items = items,
                indiceSeleccionado = indice,
                enSeleccionar = { i ->
                    when (i) {
                        0 -> onNavigateToInicio()
                        2 -> if (esCoord) onEquipoClick() else onNavigateToAgenda()
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onSouthbound, modifier = Modifier.fillMaxWidth()) {
                Text(textoSouthbound("Consultar servicios Southbound", "Browse Southbound services"))
            }
            LinceSearchBar(
                valor = busqueda,
                enCambioValor = { busqueda = it },
                pista = t.servicesSearch
            )
            val seleccionadoLabel = when (filtro) {
                null -> t.servicesAllFilter
                EstadoServicio.PENDIENTE -> t.servicesPendingFilter
                EstadoServicio.CONFIRMADA -> t.servicesConfirmedFilter
                else -> t.servicesAllFilter
            }
            LinceFilterChipRow(
                items = listOf(
                    t.servicesAllFilter to 0,
                    t.servicesPendingFilter to 0,
                    t.servicesConfirmedFilter to 0
                ),
                seleccionado = seleccionadoLabel,
                enSeleccionar = { label ->
                    filtro = when (label) {
                        t.servicesPendingFilter -> EstadoServicio.PENDIENTE
                        t.servicesConfirmedFilter -> EstadoServicio.CONFIRMADA
                        else -> null
                    }
                }
            )
            if (filtrados.isEmpty()) {
                LinceCard(
                    fondo = Colores.superficieNeutra,
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
    LinceCard(
        forma = RoundedCornerShape(18.dp),
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
