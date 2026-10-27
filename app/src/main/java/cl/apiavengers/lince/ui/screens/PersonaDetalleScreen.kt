@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.Usuario
import cl.apiavengers.lince.ui.components.BarraSuperior
import cl.apiavengers.lince.ui.components.ChipDisponibilidad
import cl.apiavengers.lince.ui.components.ChipEstadoServicio
import cl.apiavengers.lince.ui.components.ChipIdioma
import cl.apiavengers.lince.ui.fechaNumerica
import cl.apiavengers.lince.ui.montoClp
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun PersonaDetalleScreen(
    vm: LinceViewModel,
    personaId: String,
    onBack: () -> Unit,
    onServicioClick: (String) -> Unit
) {
    val persona = remember(vm.equipo) { vm.obtenerPersona(personaId) }
    val servicios = remember(vm.servicios) { vm.serviciosDe(personaId) }
    val calificacion = remember(vm.historial) { vm.promedioCalificacion(personaId) }
    val total = remember(vm.historial, vm.servicios) { vm.totalGanado(personaId) }

    Scaffold(
        topBar = {
            BarraSuperior(titulo = "Ficha del equipo", onBack = onBack)
        }
    ) { innerPadding ->
        if (persona == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No se encontró la persona.")
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            persona.nombre.take(1),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(persona.nombre, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("${persona.rol.etiqueta} · ${persona.id}", fontSize = 13.sp)
                    ChipDisponibilidad(persona.disponibilidad)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                persona.idiomas.forEach { ChipIdioma(it) }
            }

            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Contacto", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Fila("Correo", persona.correo)
                    Fila("Teléfono", persona.telefono)
                    Fila("Zona", persona.zona)
                }
            }

            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Desempeño", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            if (calificacion != null) " ${"%.1f".format(calificacion)}/5" else " Sin calificaciones",
                            fontSize = 14.sp
                        )
                    }
                    Fila("Acumulado", montoClp(total))
                }
            }

            Text("Servicios activos", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            if (servicios.isEmpty()) {
                Text("Sin servicios activos asignados.", fontSize = 14.sp)
            }
            servicios.forEach { servicio ->
                OutlinedCard(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onServicioClick(servicio.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(servicio.titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                "${fechaNumerica(servicio.fecha)} · ${servicio.hora}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ChipEstadoServicio(servicio.estado)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(14.dp))
                Text(" Toca un servicio para ver el detalle.", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
