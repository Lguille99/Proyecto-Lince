@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.apiavengers.lince.ui.components.BarraSuperior
import cl.apiavengers.lince.ui.idioma.IdiomaApp
import cl.apiavengers.lince.ui.idioma.PreferenciaIdioma
import cl.apiavengers.lince.viewmodel.SouthboundViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun textoSouthbound(es: String, en: String): String {
    val idioma by PreferenciaIdioma.idioma.collectAsStateWithLifecycle()
    return if (idioma == IdiomaApp.INGLES) en else es
}

@Composable
private fun estadoSouthbound(codigo: String?, asignacion: Boolean = false): String = when (codigo) {
    "CF" -> textoSouthbound("Confirmado", "Confirmed")
    "RQ" -> textoSouthbound("En solicitud", "Requested")
    "WL" -> textoSouthbound("Lista de espera", "Waiting list")
    "XX", "CX" -> textoSouthbound("Cancelado", "Cancelled")
    "DL" -> textoSouthbound("Eliminado", "Deleted")
    null -> textoSouthbound(if (asignacion) "Sin asignar" else "Sin información", if (asignacion) "Unassigned" else "No information")
    else -> codigo
}

@Composable
fun SouthboundScreen(
    vm: SouthboundViewModel,
    onBack: () -> Unit,
    onDetalle: (Int) -> Unit = {},
    servicioId: Int? = null
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    var busqueda by rememberSaveable { mutableStateOf(estado.busqueda) }
    LaunchedEffect(servicioId) {
        if (servicioId == null && estado.pagina == null) vm.cargar()
        else if (servicioId != null) vm.detalle(servicioId)
    }
    Scaffold(topBar = {
        BarraSuperior(
            titulo = textoSouthbound("Servicios Southbound", "Southbound services"),
            subtitulo = textoSouthbound("Consulta de datos del curso", "Course data browser"),
            onBack = onBack
        )
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(textoSouthbound(
                    "Datos ficticios de Southbound · Solo consulta. Las acciones de la app se guardan por separado.",
                    "Southbound sample data · Read only. App actions are stored separately."
                ), style = MaterialTheme.typography.bodySmall)
            }
            if (servicioId == null) {
                item {
                    OutlinedTextField(
                        value = busqueda, onValueChange = { busqueda = it },
                        label = { Text(textoSouthbound("Buscar servicios", "Search services")) },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = { vm.cargar(busqueda = busqueda) }, enabled = !estado.cargando) {
                        Text(textoSouthbound("Buscar", "Search"))
                    }
                    Text(textoSouthbound("Estado del servicio", "Service status"))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("", "CF", "RQ", "WL", "XX").forEach { codigo ->
                            FilterChip(
                                selected = estado.filtro == codigo,
                                onClick = { vm.cargar(busqueda = busqueda, filtro = codigo) },
                                enabled = !estado.cargando,
                                label = { Text(if (codigo.isEmpty()) textoSouthbound("Todos", "All") else estadoSouthbound(codigo)) }
                            )
                        }
                    }
                }
            }
            if (estado.cargando) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            estado.error?.let { error ->
                item {
                    Text(mensajeSouthbound(error), color = MaterialTheme.colorScheme.error)
                    Button(onClick = {
                        if (servicioId == null) vm.cargar(estado.numeroPagina, estado.busqueda) else vm.detalle(servicioId)
                    }) { Text(textoSouthbound("Reintentar", "Retry")) }
                }
            }
            if (estado.esCopia) item {
                Text(textoSouthbound(
                    "No se pudo actualizar. Mostrando copia guardada: ",
                    "Unable to refresh. Showing saved copy: "
                ) + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(estado.guardadoEn)),
                    color = MaterialTheme.colorScheme.tertiary)
            }
            estado.pagina?.let { pagina ->
                item {
                    Text("${pagina.total} " + textoSouthbound("resultados", "results"))
                    OutlinedButton(onClick = { vm.cargar(pagina.pagina) }, enabled = !estado.cargando) {
                        Text(textoSouthbound("Actualizar", "Refresh"))
                    }
                }
                if (pagina.servicios.isEmpty()) item {
                    Text(textoSouthbound("No hay servicios para esta búsqueda.", "No services match this search."))
                }
                items(pagina.servicios, key = { it.id }) { servicio ->
                    OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { onDetalle(servicio.id) }) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(servicio.codigo, style = MaterialTheme.typography.labelLarge)
                            Text(servicio.descripcion, style = MaterialTheme.typography.titleMedium)
                            Text("${servicio.fecha} · ${servicio.hora}")
                            Text("${servicio.recogida} → ${servicio.destino}")
                            Text(textoSouthbound("Servicio: ", "Service: ") + estadoSouthbound(servicio.estadoServicio))
                            Text(textoSouthbound("Asignación: ", "Assignment: ") + estadoSouthbound(servicio.estadoAsignacion, true))
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.cargar(pagina.pagina - 1) }, enabled = pagina.pagina > 1 && !estado.cargando) {
                            Text(textoSouthbound("Anterior", "Previous"))
                        }
                        OutlinedButton(onClick = { vm.cargar(pagina.pagina + 1) }, enabled = pagina.pagina < pagina.paginas && !estado.cargando) {
                            Text(textoSouthbound("Siguiente", "Next"))
                        }
                    }
                    Text(textoSouthbound("Página ", "Page ") + "${pagina.pagina} / ${pagina.paginas.coerceAtLeast(1)}")
                }
            }
            estado.detalle?.let { s ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(s.codigo, style = MaterialTheme.typography.labelLarge)
                        Text(s.descripcion, style = MaterialTheme.typography.headlineSmall)
                        Text("${s.fecha} · ${s.hora}")
                        Text(textoSouthbound("Tipo: ", "Type: ") + s.tipo)
                        Text(textoSouthbound("Servicio: ", "Service: ") + estadoSouthbound(s.estadoServicio))
                        Text(textoSouthbound("Asignación: ", "Assignment: ") + estadoSouthbound(s.estadoAsignacion, true))
                        Text(textoSouthbound("Estado de reserva: ", "Booking status: ") + estadoSouthbound(s.estadoReserva))
                        HorizontalDivider()
                        Text(textoSouthbound("Recogida: ", "Pickup: ") + s.recogida)
                        Text(textoSouthbound("Destino: ", "Dropoff: ") + s.destino)
                        Text(textoSouthbound("Reserva: ", "Booking: ") + s.reserva)
                        Text(textoSouthbound("Pasajero: ", "Passenger: ") + s.pasajero)
                        Text(textoSouthbound("Pasajeros: ", "Passengers: ") + s.pasajeros)
                        Text(textoSouthbound("Cliente: ", "Client: ") + s.cliente)
                        Text(textoSouthbound("Oficina: ", "Office: ") + s.oficina)
                        Text(textoSouthbound("Guía: ", "Guide: ") + (s.guia ?: "—"))
                        Text(textoSouthbound("Transportista: ", "Driver: ") + (s.transportista ?: "—"))
                        Text(textoSouthbound("Vehículo: ", "Vehicle: ") + (s.vehiculo ?: "—"))
                        Text(textoSouthbound("Idioma: ", "Language: ") + (s.idioma ?: "—"))
                        s.notas?.let { Text(it) }
                        OutlinedButton(onClick = { vm.detalle(s.id) }, enabled = !estado.cargando) {
                            Text(textoSouthbound("Actualizar", "Refresh"))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun mensajeSouthbound(codigo: String): String = when (codigo) {
    "configuracion" -> textoSouthbound("Falta configurar la clave de Southbound.", "The Southbound key is not configured.")
    "clave" -> textoSouthbound("La clave no es válida o ya no tiene acceso.", "The key is invalid or no longer has access.")
    "no_encontrado" -> textoSouthbound("El servicio no existe.", "The service does not exist.")
    "limite" -> textoSouthbound("Demasiadas consultas. Intenta nuevamente en unos momentos.", "Too many requests. Try again shortly.")
    "servidor" -> textoSouthbound("Southbound no está disponible. Intenta más tarde.", "Southbound is unavailable. Try again later.")
    "conexion" -> textoSouthbound("No se pudo conectar y no hay una copia guardada de esta consulta.", "Unable to connect and no saved copy exists for this query.")
    "formato" -> textoSouthbound("La respuesta de Southbound tiene un formato inesperado.", "Southbound returned an unexpected response format.")
    else -> textoSouthbound("No se pudo cargar la información. Intenta nuevamente.", "Unable to load data. Please try again.")
}
