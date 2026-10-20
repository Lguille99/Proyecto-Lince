package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.ResumenCoordinador
import cl.apiavengers.lince.viewmodel.LinceViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenCoordinacionScreen(
    vm: LinceViewModel,
    resumen: ResumenCoordinador?,
    onNavigateToServicios: () -> Unit = {},
    onNavigateToHistorial: () -> Unit = {},
    onNavigateToEquipo: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {}
) {
    var sincronizando by remember { mutableStateOf(false) }
    var ultimaSync by remember { mutableStateOf("aún sin sincronizar") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen operativo") },
                actions = {
                    IconButton(
                        onClick = {
                            sincronizando = true
                        },
                        enabled = !sincronizando
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = {}
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
        if (sincronizando) {
            LaunchedEffect(Unit) {
                delay(900)
                vm.sincronizar()
                ultimaSync = "hace un instante"
                sincronizando = false
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Hola, ${vm.usuario?.nombre ?: "coordinación"}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Estado de la operación turística · sincronizado $ultimaSync",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("SERVICIOS ACTIVOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                        Text("${resumen?.serviciosActivos ?: 0} en curso", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        Text(
                            "${resumen?.pendientes ?: 0} pendientes de respuesta · ${resumen?.historialTotal ?: 0} en historial",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            if (sincronizando) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaStat(
                        titulo = "Pendientes",
                        valor = "${resumen?.pendientes ?: 0}",
                        onClick = onNavigateToServicios,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaStat(
                        titulo = "Equipo",
                        valor = "${resumen?.personasEquipo ?: 0}",
                        onClick = onNavigateToEquipo,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaStat(
                        titulo = "Disponibles",
                        valor = "${resumen?.personasDisponibles ?: 0}",
                        onClick = onNavigateToEquipo,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaStat(
                        titulo = "Historial",
                        valor = "${resumen?.historialTotal ?: 0}",
                        onClick = onNavigateToHistorial,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Button(
                    onClick = onNavigateToServicios,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Ver todos los servicios", fontSize = 16.sp, fontWeight = FontWeight.Medium) }
            }
            item {
                OutlinedButton(
                    onClick = onNavigateToHistorial,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Consultar historial", fontSize = 16.sp, fontWeight = FontWeight.Medium) }
            }
            item {
                OutlinedButton(
                    onClick = onNavigateToEquipo,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) { Text("Ver equipo (guías y conductores)", fontSize = 15.sp, fontWeight = FontWeight.Medium) }
            }
        }
    }
}

@Composable
private fun TarjetaStat(titulo: String, valor: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(valor, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(titulo, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text("Toca para abrir", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}
