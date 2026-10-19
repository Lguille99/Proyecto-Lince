package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.Disponibilidad
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.modelo.Usuario

@Composable
fun EquipoScreen(
    equipo: List<Usuario>,
    onPersonaClick: (String) -> Unit,
    onNavigateToInicio: () -> Unit = {},
    onNavigateToServicios: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {}
) {
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroRol by remember { mutableStateOf("Todos") }

    val filtrados = equipo.filter { persona ->
        val coincideRol = filtroRol == "Todos" || persona.rol.etiqueta == filtroRol
        val coincideTexto = textoBusqueda.isBlank() ||
            persona.nombre.contains(textoBusqueda, ignoreCase = true) ||
            persona.id.contains(textoBusqueda, ignoreCase = true) ||
            persona.zona.contains(textoBusqueda, ignoreCase = true)
        coincideRol && coincideTexto
    }

    Scaffold(
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
                    icon = { Icon(Icons.Default.Group, contentDescription = "Equipo") },
                    label = { Text("Equipo") },
                    selected = true,
                    onClick = {}
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Equipo", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${filtrados.size} personas · toca para ver en grande",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            item {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = { Text("Buscar por nombre, ID o zona") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todos", Rol.CONDUCTOR.etiqueta, Rol.GUIA.etiqueta, Rol.COORDINADOR.etiqueta).forEach { rol ->
                        FilterChip(
                            selected = filtroRol == rol,
                            onClick = { filtroRol = rol },
                            label = { Text(rol) }
                        )
                    }
                }
            }
            items(filtrados, key = { it.id }) { persona ->
                TarjetaPersona(persona = persona, onClick = { onPersonaClick(persona.id) })
            }
            if (filtrados.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Sin resultados. Prueba con otro filtro.",
                            modifier = Modifier.padding(20.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaPersona(persona: Usuario, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = persona.nombre.firstOrNull()?.uppercase() ?: "?",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(persona.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "${persona.rol.etiqueta} · ${persona.id}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                EstadoChip(persona.disponibilidad)
            }
            Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EstadoChip(disponibilidad: Disponibilidad) {
    val (icono, color) = when (disponibilidad) {
        Disponibilidad.DISPONIBLE -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        Disponibilidad.EN_SERVICIO -> Icons.Default.Schedule to MaterialTheme.colorScheme.secondary
        Disponibilidad.NO_DISPONIBLE -> Icons.Default.Cancel to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icono, contentDescription = disponibilidad.etiqueta, tint = color, modifier = Modifier.size(14.dp))
            Text(disponibilidad.etiqueta, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}
