@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.apiavengers.lince.modelo.Usuario
import cl.apiavengers.lince.ui.components.ChipDisponibilidad
import cl.apiavengers.lince.ui.components.ChipIdioma
import cl.apiavengers.lince.ui.components.EstadoVacio
import cl.apiavengers.lince.ui.components.LinceCard
import cl.apiavengers.lince.ui.components.LinceFloatingBottomBar
import cl.apiavengers.lince.ui.components.LinceSearchBar
import cl.apiavengers.lince.ui.components.LinceTopBar
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.ui.theme.Colores
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun EquipoScreen(
    vm: LinceViewModel,
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
    var busqueda by remember { mutableStateOf("") }
    val esCoord = vm.esCoordinador()
    val equipo = remember(estado.equipo, busqueda) {
        estado.equipo.filter {
            it.nombre.contains(busqueda, true) || t.rol(it.rol).contains(busqueda, true)
        }
    }

    Scaffold(
        topBar = {
            LinceTopBar(titulo = t.teamTitle, subtitulo = t.teamSubtitle, navegarAtras = onBack)
        },
        bottomBar = {
            val indice = if (esCoord) 2 else 1
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
                        1 -> onNavigateToServicios()
                        2 -> if (esCoord) onNavigateToEquipo() else onNavigateToAgenda()
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
            LinceSearchBar(
                valor = busqueda,
                enCambioValor = { busqueda = it },
                pista = t.teamSearch
            )
            equipo.forEach { persona ->
                TarjetaPersona(
                    persona = persona,
                    calificacion = vm.promedioCalificacion(persona.id),
                    onClick = { onPersonaClick(persona.id) }
                )
            }
            if (equipo.isEmpty()) {
                EstadoVacio(
                    titulo = t.teamNoMatches,
                    mensaje = t.teamNoCriteria,
                    icono = Icons.Default.Person,
                    accionTexto = if (busqueda.isNotBlank()) t.clearSearch else null,
                    onAccion = if (busqueda.isNotBlank()) ({ busqueda = "" }) else null
                )
            }
        }
    }
}

@Composable
fun TarjetaPersona(persona: Usuario, calificacion: Double?, onClick: () -> Unit) {
    val t = textos()
    LinceCard(
        forma = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        persona.nombre.take(1),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(persona.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${t.rol(persona.rol)} · ${persona.zona}", fontSize = 12.sp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    persona.idiomas.forEach { ChipIdioma(it) }
                }
                if (calificacion != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(" ${"%.1f".format(calificacion)}/5", fontSize = 12.sp)
                    }
                }
            }
            ChipDisponibilidad(persona.disponibilidad)
        }
    }
}
