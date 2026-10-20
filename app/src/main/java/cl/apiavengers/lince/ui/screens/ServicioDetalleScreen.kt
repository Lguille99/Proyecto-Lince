package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun ServicioDetalleScreen(
    servicio: Servicio?,
    responsable: Usuario?,
    onBack: () -> Unit,
    onResponsableClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de servicio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (servicio == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Servicio no encontrado.", color = MaterialTheme.colorScheme.error)
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
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${servicio.fecha} · ${servicio.hora}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                        Text(servicio.titulo, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        Text(servicio.tipo, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                        ChipEstadoServicio(servicio.estado)
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ruta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Recogida: ${servicio.recogida}", fontSize = 15.sp)
                        Text("Destino: ${servicio.destino}", fontSize = 15.sp)
                        Text("ID: ${servicio.id}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            item {
                Text("Responsable (toca para ver en grande)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            item {
                if (responsable != null) {
                    TarjetaPersona(persona = responsable, onClick = { onResponsableClick(responsable.id) })
                } else {
                    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Asignado a: ${servicio.asignadoA}", Modifier.padding(16.dp))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Confirmar") }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Rechazar") }
                }
                Text(
                    "Botones visuales del MVP. La lógica de respuesta se conecta en el siguiente sprint.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}
