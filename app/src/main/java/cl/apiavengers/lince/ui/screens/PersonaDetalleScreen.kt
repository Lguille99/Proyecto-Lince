package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaDetalleScreen(
    persona: Usuario?,
    serviciosAsignados: List<Servicio>,
    onBack: () -> Unit,
    onServicioClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de persona") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (persona == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Persona no encontrada.", color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(72.dp)) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    persona.nombre.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(persona.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        Text(
                            "${persona.rol.etiqueta} · ${persona.id}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                        EstadoChipGrande(persona)
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Contacto y zona", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        DetalleFila(Icons.Default.Phone, "Teléfono", persona.telefono)
                        DetalleFila(Icons.Default.Email, "Correo", persona.correo)
                        DetalleFila(Icons.Default.Badge, "Cuenta", persona.cuenta)
                        DetalleFila(Icons.Default.Place, "Zona", persona.zona)
                    }
                }
            }
            item {
                Text("Servicios asignados (${serviciosAsignados.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            if (serviciosAsignados.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sin servicios asignados por ahora.", Modifier.padding(16.dp), fontSize = 14.sp)
                    }
                }
            } else {
                items(serviciosAsignados.size) { index ->
                    val s = serviciosAsignados[index]
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onServicioClick(s.id) }
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("${s.fecha} · ${s.hora}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(s.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("${s.recogida} → ${s.destino}", fontSize = 13.sp)
                            }
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Ver servicio", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoChipGrande(persona: Usuario) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.onPrimary) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EstadoChip(persona.disponibilidad)
        }
    }
}

@Composable
private fun DetalleFila(icono: androidx.compose.ui.graphics.vector.ImageVector, etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icono, contentDescription = etiqueta, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(etiqueta, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(valor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
