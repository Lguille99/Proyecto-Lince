@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.ui.components.BarraSuperior
import cl.apiavengers.lince.ui.components.BotonPrincipal
import cl.apiavengers.lince.ui.components.BotonSecundario
import cl.apiavengers.lince.ui.components.ChipDisponibilidad
import cl.apiavengers.lince.viewmodel.LinceViewModel
import kotlinx.coroutines.delay

@Composable
fun ResumenCoordinacionScreen(
    vm: LinceViewModel,
    onServicios: () -> Unit,
    onHistorial: () -> Unit,
    onEquipo: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val resumen = vm.resumen
    val snackbar = remember { SnackbarHostState() }
    var sincronizando by remember { mutableStateOf(false) }
    val mensaje = vm.mensaje

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbar.showSnackbar(mensaje)
            vm.limpiarMensaje()
        }
    }
    LaunchedEffect(sincronizando) {
        if (sincronizando) {
            delay(600)
            vm.sincronizar()
            sincronizando = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            BarraSuperior(
                titulo = "Resumen operativo",
                subtitulo = "Coordinación · Datos de prueba",
                acciones = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Estado de las asignaciones", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    FilaResumen(
                        valor = resumen?.pendientes ?: 0,
                        etiqueta = "Pendientes de respuesta",
                        contenedor = MaterialTheme.colorScheme.primaryContainer,
                        contenido = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    FilaResumen(
                        valor = resumen?.confirmadas ?: 0,
                        etiqueta = "Confirmadas",
                        contenedor = MaterialTheme.colorScheme.secondaryContainer,
                        contenido = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    FilaResumen(
                        valor = resumen?.rechazadas ?: 0,
                        etiqueta = "Rechazadas",
                        contenedor = MaterialTheme.colorScheme.errorContainer,
                        contenido = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        "${resumen?.serviciosActivos ?: 0} servicios activos · " +
                            "${resumen?.enEjecucion ?: 0} en ejecución · " +
                            "${resumen?.historialTotal ?: 0} en historial",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            BotonPrincipal(texto = "Ver todos los servicios", onClick = onServicios)
            BotonSecundario(texto = "Consultar historial", onClick = onHistorial)

            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Disponibilidad de recursos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${resumen?.personasDisponibles ?: 0}/${resumen?.personasEquipo ?: 0}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val disponibles = resumen?.personasDisponibles ?: 0
                    val totalEquipo = resumen?.personasEquipo ?: 0
                    LinearProgressIndicator(
                        progress = { if (totalEquipo > 0) disponibles.toFloat() / totalEquipo else 0f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Text(
                        "$disponibles de $totalEquipo disponibles ahora",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    vm.equipo.take(4).forEach { persona ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(persona.nombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(persona.rol.etiqueta, fontSize = 12.sp)
                            }
                            ChipDisponibilidad(persona.disponibilidad)
                        }
                    }
                    TextButton(onClick = onEquipo) { Text("Ver equipo completo") }
                }
            }

            BotonPrincipal(
                texto = if (sincronizando) "Sincronizando..." else "Simular sincronización",
                onClick = { sincronizando = true },
                cargando = sincronizando
            )
            Text(
                "Última simulación: ${vm.ultimaSincronizacion ?: "sin sincronizar"}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaResumen(valor: Int, etiqueta: String, contenedor: Color, contenido: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = contenedor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).background(contenido, CircleShape))
            Text(valor.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = contenido)
            Text(etiqueta, fontSize = 14.sp, color = contenido)
        }
    }
}
