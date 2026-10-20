package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.*

@Composable
fun ServiciosScreen(
    usuario: Usuario,
    servicios: List<Servicio>,
    onNavigateToInicio: () -> Unit = {},
    onNavigateToEquipo: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onServicioClick: (String) -> Unit = {}
) {
    var selectedBottomItem by remember { mutableStateOf(1) }
    
    var filtroSeleccionado by remember { mutableStateOf("Todas") }
    var textoBusqueda by remember { mutableStateOf("") }

    val bottomItems = listOf("Inicio", "Servicios", "Agenda", "Perfil")
    val bottomIcons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.List,
        Icons.Default.DateRange,
        Icons.Default.Person
    )

    val serviciosFiltrados = servicios.filter { servicio ->
        val coincideFiltro = when (filtroSeleccionado) {
            "Pendientes" -> servicio.titulo.contains("mañana", ignoreCase = true)
             || servicio.destino.contains("prueba", ignoreCase = true)
            "Confirmadas" -> servicio.titulo.contains("Excursión", ignoreCase = true)
            else -> true
        }
        val coincideTexto = textoBusqueda.isBlank() || 
            servicio.titulo.contains(textoBusqueda, ignoreCase = true) ||
            servicio.recogida.contains(textoBusqueda, ignoreCase = true)
        
        coincideFiltro && coincideTexto
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                bottomItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(bottomIcons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedBottomItem == index,
                        onClick = {
                            selectedBottomItem = index
                            when (index) {
                                0 -> onNavigateToInicio()
                                2 -> onNavigateToAgenda()
                                3 -> onNavigateToPerfil()
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Mis servicios",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (usuario.rol == Rol.COORDINADOR) "Todos los servicios del sistema" else "Asignaciones del conductor",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(
                        onClick = onNavigateToEquipo,
                        modifier = Modifier
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Ver equipo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = { Text("Todos los servicios") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filtros = listOf("Todas", "Pendientes", "Confirmadas")
                    filtros.forEach { filtro ->
                        val seleccionado = filtroSeleccionado == filtro
                        FilterChip(
                            selected = seleccionado,
                            onClick = { filtroSeleccionado = filtro },
                            label = { Text(filtro) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            items(serviciosFiltrados, key = { it.id }) { servicio ->
                TarjetaServicio(servicio, usuario.rol == Rol.COORDINADOR, onClick = { onServicioClick(servicio.id) })
            }
        }
    }
}

@Composable
private fun TarjetaServicio(servicio: Servicio, mostrarRecurso: Boolean, onClick: () -> Unit = {}) {
    val estadoTexto = servicio.estado.etiqueta

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${servicio.fecha} · ${servicio.hora}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = servicio.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${servicio.recogida} → ${servicio.destino}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (mostrarRecurso) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Asignado a: ${servicio.asignadoA}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = estadoTexto,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}