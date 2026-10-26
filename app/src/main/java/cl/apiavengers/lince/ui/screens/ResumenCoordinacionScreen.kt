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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            TopAppBar(
                title = {
                    Column {
                        Text("Resumen operativo", fontWeight = FontWeight.Bold)
                        Text("Coordinación · Datos de prueba", fontSize = 12.sp)
                    }
                },
                actions = {
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
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Estado de las asignaciones", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    FilaResumen(
                        valor = resumen?.pendientes ?: 0,
                        etiqueta = "Pendientes de respuesta",
                        color = MaterialTheme.colorScheme.primary
                    )
                    FilaResumen(
                        valor = resumen?.confirmadas ?: 0,
                        etiqueta = "Confirmadas",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    FilaResumen(
                        valor = resumen?.rechazadas ?: 0,
                        etiqueta = "Rechazadas",
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "${resumen?.serviciosActivos ?: 0} servicios activos · " +
                            "${resumen?.enEjecucion ?: 0} en ejecución · " +
                            "${resumen?.historialTotal ?: 0} en historial",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            BotonPrincipal(texto = "Ver todos los servicios", onClick = onServicios)
            BotonSecundario(texto = "Consultar historial", onClick = onHistorial)

            Card(
                shape = RoundedCornerShape(18.dp),
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun FilaResumen(valor: Int, etiqueta: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(valor.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(etiqueta, fontSize = 14.sp)
    }
}
