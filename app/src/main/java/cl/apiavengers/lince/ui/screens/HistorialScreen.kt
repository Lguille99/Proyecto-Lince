package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Servicio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    historial: List<Servicio>,
    onBack: () -> Unit,
    onServicioClick: (String) -> Unit,
    onNavigateToInicio: () -> Unit = {},
    onNavigateToServicios: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = onNavigateToInicio
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Servicios") },
                    label = { Text("Servicios") },
                    selected = false,
                    onClick = onNavigateToServicios
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Agenda") },
                    label = { Text("Agenda") },
                    selected = false,
                    onClick = onNavigateToAgenda
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = onNavigateToPerfil
                )
            }
        }
    ) { innerPadding ->
        if (historial.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Sin servicios en el historial todavía.")
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "${historial.size} servicios finalizados · toca para ver en grande",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            items(historial.size) { index ->
                val servicio = historial[index]
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onServicioClick(servicio.id) }
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${servicio.fecha} · ${servicio.hora}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(servicio.titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${servicio.recogida} → ${servicio.destino}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Spacer(Modifier.height(2.dp))
                        ChipEstadoServicio(servicio.estado)
                    }
                }
            }
        }
    }
}

@Composable
fun ChipEstadoServicio(estado: EstadoServicio) {
    val (icono, color) = when (estado) {
        EstadoServicio.COMPLETADA -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.secondary
        EstadoServicio.RECHAZADA -> Icons.Default.Close to MaterialTheme.colorScheme.error
        EstadoServicio.CONFIRMADA -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        EstadoServicio.PENDIENTE -> Icons.Default.DateRange to MaterialTheme.colorScheme.primary
    }
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icono, contentDescription = estado.etiqueta, tint = color, modifier = Modifier.size(14.dp))
            Text(estado.etiqueta, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}
