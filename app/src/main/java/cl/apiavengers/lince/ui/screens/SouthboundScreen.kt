@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.apiavengers.lince.datos.southbound.*
import cl.apiavengers.lince.ui.components.LinceCard
import cl.apiavengers.lince.ui.components.LinceInputField
import cl.apiavengers.lince.ui.components.LinceSearchBar
import cl.apiavengers.lince.ui.components.LinceTopBar
import cl.apiavengers.lince.ui.fechaCorta
import cl.apiavengers.lince.ui.idioma.IdiomaApp
import cl.apiavengers.lince.ui.idioma.PreferenciaIdioma
import cl.apiavengers.lince.ui.selloFechaHora
import cl.apiavengers.lince.viewmodel.*
import java.text.DateFormat
import java.util.Date

private val FORMATO_SELLO_CORTO: DateFormat =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

@Composable
fun textoSouthbound(es: String, en: String): String {
    val idioma by PreferenciaIdioma.idioma.collectAsStateWithLifecycle()
    return if (idioma == IdiomaApp.INGLES) en else es
}

@Composable
private fun estadoServicioTexto(codigo: String?): String = when (codigo) {
    "CF" -> textoSouthbound("Confirmado", "Confirmed")
    "RQ" -> textoSouthbound("En solicitud", "Requested")
    "WL" -> textoSouthbound("Lista de espera", "Waiting list")
    "XX" -> textoSouthbound("Cancelado", "Cancelled")
    null -> textoSouthbound("Sin información", "No information")
    else -> codigo
}

@Composable
private fun estadoAsignacionTexto(codigo: String?): String = when (codigo) {
    "CF" -> textoSouthbound("Confirmado por staff", "Confirmed by staff")
    "RQ" -> textoSouthbound("En solicitud", "Requested")
    "DL" -> textoSouthbound("Eliminado", "Deleted")
    "CX" -> textoSouthbound("Cancelado", "Cancelled")
    null -> textoSouthbound("Sin asignar", "Unassigned")
    else -> codigo
}

@Composable
private fun tipoTexto(tipo: String): String = when (tipo) {
    "TRANSFER" -> textoSouthbound("Transfer", "Transfer")
    "GUIDED_SERVICE" -> textoSouthbound("Servicio guiado", "Guided service")
    "EXCURSION" -> textoSouthbound("Excursión", "Excursion")
    else -> tipo
}

@Composable
private fun nombreSeccion(seccion: SeccionSouthbound): String = when (seccion) {
    SeccionSouthbound.SERVICIOS -> textoSouthbound("Servicios", "Services")
    SeccionSouthbound.GUIAS -> textoSouthbound("Guías", "Guides")
    SeccionSouthbound.TRANSPORTISTAS -> textoSouthbound("Transportistas", "Drivers")
    SeccionSouthbound.CLIENTES -> textoSouthbound("Clientes", "Clients")
    SeccionSouthbound.RESUMEN -> textoSouthbound("Resumen", "Summary")
}

@Composable
fun SouthboundScreen(
    vm: SouthboundViewModel,
    onBack: () -> Unit,
    onDetalle: (Int) -> Unit = {},
    servicioId: Int? = null
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LaunchedEffect(servicioId, estado.iniciado) {
        if (servicioId != null) {
            vm.detalleServicio(servicioId)
        } else if (!estado.iniciado) {
            vm.iniciar()
        }
    }
    Scaffold(topBar = {
        LinceTopBar(
            titulo = textoSouthbound("Lince API · Southbound", "Lince API · Southbound"),
            subtitulo = textoSouthbound("Consulta de datos del curso", "Course data browser"),
            navegarAtras = onBack
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (servicioId != null) {
                Box(Modifier.weight(1f)) {
                    DetalleServicioContenido(estado, vm)
                }
            } else {
                val secciones = SeccionSouthbound.entries
                ScrollableTabRow(
                    selectedTabIndex = secciones.indexOf(estado.seccion),
                    edgePadding = 8.dp
                ) {
                    secciones.forEach { seccion ->
                        Tab(
                            selected = estado.seccion == seccion,
                            onClick = { vm.seleccionar(seccion) },
                            text = { Text(nombreSeccion(seccion)) }
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    when (estado.seccion) {
                        SeccionSouthbound.SERVICIOS -> ContenidoServicios(estado, vm, onDetalle)
                        SeccionSouthbound.GUIAS -> if (estado.detalleGuia != null) {
                            DetalleGuiaContenido(estado.detalleGuia!!, vm)
                        } else {
                            ContenidoGuias(estado, vm)
                        }
                        SeccionSouthbound.TRANSPORTISTAS -> if (estado.detalleTransportista != null) {
                            DetalleTransportistaContenido(estado.detalleTransportista!!, vm)
                        } else {
                            ContenidoTransportistas(estado, vm)
                        }
                        SeccionSouthbound.CLIENTES -> if (estado.detalleCliente != null) {
                            DetalleClienteContenido(estado.detalleCliente!!, vm)
                        } else {
                            ContenidoClientes(estado, vm)
                        }
                        SeccionSouthbound.RESUMEN -> ContenidoResumen(estado, vm)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContenidoServicios(estado: SouthboundUiState, vm: SouthboundViewModel, onDetalle: (Int) -> Unit) {
    val filtros = estado.filtrosServicios
    var busqueda by rememberSaveable { mutableStateOf(filtros.busqueda) }
    var fechaDesde by rememberSaveable { mutableStateOf(filtros.fechaDesde) }
    var fechaHasta by rememberSaveable { mutableStateOf(filtros.fechaHasta) }
    val aplicar = { pagina: Int, estadoServicio: String, statusPL: String, tipo: String, oficina: String ->
        vm.cargarServicios(
            pagina, busqueda, estadoServicio, statusPL, tipo, oficina, fechaDesde, fechaHasta
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AvisoDatos() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinceSearchBar(
                    valor = busqueda,
                    enCambioValor = { busqueda = it },
                    pista = textoSouthbound("Buscar (código, reserva, descripción)", "Search (code, booking, description)")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoFecha(fechaDesde, { fechaDesde = it }, textoSouthbound("Desde", "From"), Modifier.weight(1f))
                    CampoFecha(fechaHasta, { fechaHasta = it }, textoSouthbound("Hasta", "To"), Modifier.weight(1f))
                }
                Button(
                    onClick = { aplicar(1, filtros.estadoServicio, filtros.statusPL, filtros.tipo, filtros.oficina) },
                    enabled = !estado.cargando
                ) { Text(textoSouthbound("Aplicar filtros", "Apply filters")) }
                Text(textoSouthbound("Fecha (SrvDate). Formato AAAA-MM-DD", "Date (SrvDate). Format YYYY-MM-DD"), fontSize = 12.sp)
            }
        }
        item {
            FilaChips(
                textoSouthbound("Estado de servicio / reserva", "Service / booking status"),
                listOf("" to textoSouthbound("Todos", "All")) + listOf(
                    "CF" to estadoServicioTexto("CF"),
                    "RQ" to estadoServicioTexto("RQ"),
                    "WL" to estadoServicioTexto("WL"),
                    "XX" to estadoServicioTexto("XX")
                ),
                filtros.estadoServicio,
                !estado.cargando
            ) { aplicar(1, it, filtros.statusPL, filtros.tipo, filtros.oficina) }
        }
        item {
            FilaChips(
                textoSouthbound("Asignación (StatusPL)", "Assignment (StatusPL)"),
                listOf("" to textoSouthbound("Todos", "All")) + listOf(
                    "CF" to estadoAsignacionTexto("CF"),
                    "RQ" to estadoAsignacionTexto("RQ"),
                    "DL" to estadoAsignacionTexto("DL"),
                    "CX" to estadoAsignacionTexto("CX"),
                    "SIN_ASIGNAR" to estadoAsignacionTexto(null)
                ),
                filtros.statusPL,
                !estado.cargando
            ) { aplicar(1, filtros.estadoServicio, it, filtros.tipo, filtros.oficina) }
        }
        val oficinas = estado.catalogo?.oficinas.orEmpty()
        if (oficinas.isNotEmpty()) {
            item {
                FilaChips(
                    textoSouthbound("Oficina", "Office"),
                    listOf("" to textoSouthbound("Todas", "All")) + oficinas.map { it.codigo to it.nombre },
                    filtros.oficina,
                    !estado.cargando
                ) { aplicar(1, filtros.estadoServicio, filtros.statusPL, filtros.tipo, it) }
            }
        }
        val tipos = estado.catalogo?.tiposServicio.orEmpty()
        if (tipos.isNotEmpty()) {
            item {
                FilaChips(
                    textoSouthbound("Tipo de servicio", "Service type"),
                    listOf("" to textoSouthbound("Todos", "All")) + tipos.map { it.tipo to tipoTexto(it.tipo) },
                    filtros.tipo,
                    !estado.cargando
                ) { aplicar(1, filtros.estadoServicio, filtros.statusPL, it, filtros.oficina) }
            }
        }
        estadoGeneral(estado) { aplicar(1, filtros.estadoServicio, filtros.statusPL, filtros.tipo, filtros.oficina) }
        estado.servicios?.let { pagina ->
            item { TotalResultados(pagina.total) }
            if (pagina.datos.isEmpty()) item { SinResultados() }
            items(pagina.datos, key = { it.id }, contentType = { "servicio" }) { servicio ->
                TarjetaServicio(servicio) { onDetalle(servicio.id) }
            }
            item {
                Paginador(pagina.pagina, pagina.paginas, !estado.cargando,
                    onAnterior = {
                        vm.cargarServicios(
                            pagina.pagina - 1, filtros.busqueda, filtros.estadoServicio,
                            filtros.statusPL, filtros.tipo, filtros.oficina,
                            filtros.fechaDesde, filtros.fechaHasta
                        )
                    },
                    onSiguiente = {
                        vm.cargarServicios(
                            pagina.pagina + 1, filtros.busqueda, filtros.estadoServicio,
                            filtros.statusPL, filtros.tipo, filtros.oficina,
                            filtros.fechaDesde, filtros.fechaHasta
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ContenidoGuias(estado: SouthboundUiState, vm: SouthboundViewModel) {
    val filtros = estado.filtrosGuias
    var busqueda by rememberSaveable { mutableStateOf(filtros.busqueda) }
    val aplicar = { pagina: Int, oficina: String, idioma: String ->
        vm.cargarGuias(pagina, busqueda, oficina, idioma)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AvisoDatos() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinceSearchBar(
                    valor = busqueda,
                    enCambioValor = { busqueda = it },
                    pista = textoSouthbound("Buscar guía por nombre o código", "Search guide by name or code")
                )
                Button(
                    onClick = { aplicar(1, filtros.oficina, filtros.idioma) },
                    enabled = !estado.cargando
                ) { Text(textoSouthbound("Buscar", "Search")) }
            }
        }
        val oficinas = estado.catalogo?.oficinas.orEmpty()
        if (oficinas.isNotEmpty()) {
            item {
                FilaChips(
                    textoSouthbound("Oficina", "Office"),
                    listOf("" to textoSouthbound("Todas", "All")) + oficinas.map { it.codigo to it.nombre },
                    filtros.oficina,
                    !estado.cargando
                ) { aplicar(1, it, filtros.idioma) }
            }
        }
        val idiomas = estado.catalogo?.idiomas.orEmpty()
        if (idiomas.isNotEmpty()) {
            item {
                FilaChips(
                    textoSouthbound("Idioma", "Language"),
                    listOf("" to textoSouthbound("Todos", "All")) + idiomas.map { it.codigo to it.nombre },
                    filtros.idioma,
                    !estado.cargando
                ) { aplicar(1, filtros.oficina, it) }
            }
        }
        estadoGeneral(estado) { aplicar(1, filtros.oficina, filtros.idioma) }
        estado.guias?.let { pagina ->
            item { TotalResultados(pagina.total) }
            if (pagina.datos.isEmpty()) item { SinResultados() }
            items(pagina.datos, key = { it.id }, contentType = { "guia" }) { guia ->
                LinceCard(
                    modifier = Modifier.fillMaxWidth().clickable { vm.detalleGuia(guia.id) }
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(guia.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(guia.codigo, style = MaterialTheme.typography.labelMedium)
                        Text(textoSouthbound("Oficina: ", "Office: ") + guia.oficina.nombre)
                        Text(textoSouthbound("Idiomas: ", "Languages: ") + guia.idiomas.joinToString { it.codigo })
                        Text(textoSouthbound("Evaluación: ", "Rating: ") + "%.2f".format(guia.evaluacionPromedio))
                    }
                }
            }
            item {
                Paginador(pagina.pagina, pagina.paginas, !estado.cargando,
                    onAnterior = { vm.cargarGuias(pagina.pagina - 1, filtros.busqueda, filtros.oficina, filtros.idioma) },
                    onSiguiente = { vm.cargarGuias(pagina.pagina + 1, filtros.busqueda, filtros.oficina, filtros.idioma) }
                )
            }
        }
    }
}

@Composable
private fun ContenidoTransportistas(estado: SouthboundUiState, vm: SouthboundViewModel) {
    val filtros = estado.filtrosTransportistas
    var busqueda by rememberSaveable { mutableStateOf(filtros.busqueda) }
    val aplicar = { pagina: Int, oficina: String -> vm.cargarTransportistas(pagina, busqueda, oficina) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AvisoDatos() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinceSearchBar(
                    valor = busqueda,
                    enCambioValor = { busqueda = it },
                    pista = textoSouthbound("Buscar transportista por nombre o código", "Search driver by name or code")
                )
                Button(
                    onClick = { aplicar(1, filtros.oficina) },
                    enabled = !estado.cargando
                ) { Text(textoSouthbound("Buscar", "Search")) }
            }
        }
        val oficinas = estado.catalogo?.oficinas.orEmpty()
        if (oficinas.isNotEmpty()) {
            item {
                FilaChips(
                    textoSouthbound("Oficina", "Office"),
                    listOf("" to textoSouthbound("Todas", "All")) + oficinas.map { it.codigo to it.nombre },
                    filtros.oficina,
                    !estado.cargando
                ) { aplicar(1, it) }
            }
        }
        estadoGeneral(estado) { aplicar(1, filtros.oficina) }
        estado.transportistas?.let { pagina ->
            item { TotalResultados(pagina.total) }
            if (pagina.datos.isEmpty()) item { SinResultados() }
            items(pagina.datos, key = { it.id }, contentType = { "transportista" }) { transportista ->
                LinceCard(
                    modifier = Modifier.fillMaxWidth().clickable { vm.detalleTransportista(transportista.id) }
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(transportista.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(transportista.codigo, style = MaterialTheme.typography.labelMedium)
                        Text(textoSouthbound("Oficina: ", "Office: ") + transportista.oficina.nombre)
                        Text(
                            textoSouthbound("Vehículos: ", "Vehicles: ") +
                                transportista.vehiculos.joinToString { "${it.patente} (${it.tipo}, ${it.capacidad})" }
                        )
                        Text(textoSouthbound("Evaluación: ", "Rating: ") + "%.2f".format(transportista.evaluacionPromedio))
                    }
                }
            }
            item {
                Paginador(pagina.pagina, pagina.paginas, !estado.cargando,
                    onAnterior = { vm.cargarTransportistas(pagina.pagina - 1, filtros.busqueda, filtros.oficina) },
                    onSiguiente = { vm.cargarTransportistas(pagina.pagina + 1, filtros.busqueda, filtros.oficina) }
                )
            }
        }
    }
}

@Composable
private fun ContenidoClientes(estado: SouthboundUiState, vm: SouthboundViewModel) {
    val filtros = estado.filtrosClientes
    var busqueda by rememberSaveable { mutableStateOf(filtros.busqueda) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AvisoDatos() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinceSearchBar(
                    valor = busqueda,
                    enCambioValor = { busqueda = it },
                    pista = textoSouthbound("Buscar cliente por nombre o código", "Search client by name or code")
                )
                Button(
                    onClick = { vm.cargarClientes(1, busqueda) },
                    enabled = !estado.cargando
                ) { Text(textoSouthbound("Buscar", "Search")) }
            }
        }
        estadoGeneral(estado) { vm.cargarClientes(1, busqueda) }
        estado.clientes?.let { pagina ->
            item { TotalResultados(pagina.total) }
            if (pagina.datos.isEmpty()) item { SinResultados() }
            items(pagina.datos, key = { it.id }, contentType = { "cliente" }) { cliente ->
                LinceCard(
                    modifier = Modifier.fillMaxWidth().clickable { vm.detalleCliente(cliente.id) }
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(cliente.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(cliente.codigo, style = MaterialTheme.typography.labelMedium)
                        Text(textoSouthbound("País: ", "Country: ") + cliente.pais)
                    }
                }
            }
            item {
                Paginador(pagina.pagina, pagina.paginas, !estado.cargando,
                    onAnterior = { vm.cargarClientes(pagina.pagina - 1, filtros.busqueda) },
                    onSiguiente = { vm.cargarClientes(pagina.pagina + 1, filtros.busqueda) }
                )
            }
        }
    }
}

@Composable
private fun ContenidoResumen(estado: SouthboundUiState, vm: SouthboundViewModel) {
    val filtros = estado.filtrosResumen
    var fechaDesde by rememberSaveable { mutableStateOf(filtros.fechaDesde) }
    var fechaHasta by rememberSaveable { mutableStateOf(filtros.fechaHasta) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AvisoDatos() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoFecha(fechaDesde, { fechaDesde = it }, textoSouthbound("Desde", "From"), Modifier.weight(1f))
                    CampoFecha(fechaHasta, { fechaHasta = it }, textoSouthbound("Hasta", "To"), Modifier.weight(1f))
                }
                Text(textoSouthbound("Formato AAAA-MM-DD. Sin fechas se considera todo el período.", "Format YYYY-MM-DD. Blank dates include the whole period."), fontSize = 12.sp)
                Button(
                    onClick = { vm.cargarResumen(fechaDesde, fechaHasta, filtros.oficina) },
                    enabled = !estado.cargando
                ) { Text(textoSouthbound("Consultar resumen", "Load summary")) }
            }
        }
        val oficinas = estado.catalogo?.oficinas.orEmpty()
        if (oficinas.isNotEmpty()) {
            item {
                FilaChips(
                    textoSouthbound("Oficina", "Office"),
                    listOf("" to textoSouthbound("Todas", "All")) + oficinas.map { it.codigo to it.nombre },
                    filtros.oficina,
                    !estado.cargando
                ) { vm.cargarResumen(fechaDesde, fechaHasta, it) }
            }
        }
        estadoGeneral(estado) { vm.cargarResumen(fechaDesde, fechaHasta, filtros.oficina) }
        estado.resumen?.let { resumen ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        textoSouthbound("Total servicios: ", "Total services: ") + resumen.totalServicios,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        textoSouthbound("Total pasajeros: ", "Total passengers: ") + resumen.totalPax,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        textoSouthbound("Período: ", "Period: ") +
                            "${resumen.fechaDesde ?: "—"} → ${resumen.fechaHasta ?: "—"}"
                    )
                }
            }
            item { Desglose(textoSouthbound("Por estado de servicio", "By service status"), resumen.porEstadoServicio) }
            item { Desglose(textoSouthbound("Por asignación (StatusPL)", "By assignment (StatusPL)"), resumen.porStatusPL) }
            item { Desglose(textoSouthbound("Por oficina", "By office"), resumen.porOficina) }
            item { Desglose(textoSouthbound("Por tipo", "By type"), resumen.porTipo) }
            item { Desglose(textoSouthbound("Por día", "By day"), resumen.porDia) }
        }
    }
}

@Composable
private fun DetalleServicioContenido(estado: SouthboundUiState, vm: SouthboundViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AvisoDatos() }
        estadoGeneral(estado) {
            estado.detalleServicio?.let { vm.detalleServicio(it.id) }
        }
        estado.detalleServicio?.let { s ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(s.codigo, style = MaterialTheme.typography.labelLarge)
                    Text(s.descripcion, style = MaterialTheme.typography.headlineSmall)
                    s.descripcionPadre?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text("${fechaCorta(s.fecha)} · ${s.horaPickup}")
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilaDetalle(textoSouthbound("Modo (Srv)", "Mode (Srv)"), "${s.srv} · ${tipoTexto(s.tipo)}")
                    FilaDetalle(textoSouthbound("Estado servicio", "Service status"), estadoServicioTexto(s.estadoServicio))
                    FilaDetalle(textoSouthbound("Estado reserva", "Booking status"), estadoServicioTexto(s.estadoReserva))
                    FilaDetalle(textoSouthbound("Asignación", "Assignment"), estadoAsignacionTexto(s.statusPL))
                    FilaDetalle(textoSouthbound("Asignación actualizada", "Assignment updated"), selloFechaHora(s.asignacionActualizadoEn))
                }
            }
            item { HorizontalDivider() }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilaDetalle(textoSouthbound("Recogida", "Pickup"), s.pickup)
                    FilaDetalle(textoSouthbound("Destino", "Dropoff"), s.dropoff)
                    FilaDetalle(textoSouthbound("Reserva", "Booking"), s.reserva)
                    FilaDetalle(textoSouthbound("Pasajero", "Passenger"), s.pasajero)
                    FilaDetalle(textoSouthbound("Pax", "Pax"), "${s.pax.texto} (${s.pax.total})")
                    FilaDetalle(textoSouthbound("Cliente", "Client"), "${s.cliente.nombre} · ${s.cliente.pais}")
                    FilaDetalle(textoSouthbound("Oficina", "Office"), s.oficina.nombre)
                    FilaDetalle(textoSouthbound("Idioma", "Language"), s.idiomaRequerido?.nombre ?: "—")
                    FilaDetalle(textoSouthbound("Guía", "Guide"), s.guia?.let { "${it.nombre} (${it.codigo})" } ?: "—")
                    FilaDetalle(
                        textoSouthbound("Transportista", "Driver"),
                        s.transportista?.let { "${it.nombre} (${it.codigo})" } ?: "—"
                    )
                    FilaDetalle(
                        textoSouthbound("Vehículo", "Vehicle"),
                        s.vehiculo?.let { "${it.patente} · ${it.tipo} · ${it.capacidad}" } ?: "—"
                    )
                    s.notas?.let { FilaDetalle(textoSouthbound("Notas", "Notes"), it) }
                }
            }
            item {
                OutlinedButton(
                    onClick = { vm.detalleServicio(s.id) },
                    enabled = !estado.cargando
                ) { Text(textoSouthbound("Actualizar", "Refresh")) }
            }
        }
    }
}

@Composable
private fun DetalleGuiaContenido(guia: GuiaSouthbound, vm: SouthboundViewModel) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { VolverDetalle { vm.cerrarDetalle() } }
        estadoGeneral(estado) { vm.detalleGuia(guia.id) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(guia.nombre, style = MaterialTheme.typography.headlineSmall)
                FilaDetalle(textoSouthbound("Código", "Code"), guia.codigo)
                FilaDetalle(textoSouthbound("Oficina", "Office"), guia.oficina.nombre)
                FilaDetalle(textoSouthbound("Idiomas", "Languages"), guia.idiomas.joinToString { "${it.codigo} (${it.nombre})" })
                FilaDetalle(textoSouthbound("Evaluación", "Rating"), "%.2f".format(guia.evaluacionPromedio))
                FilaDetalle(textoSouthbound("Fecha de ingreso", "Hire date"), guia.fechaIngreso)
                FilaDetalle(textoSouthbound("Activo", "Active"), textoSouthbound(if (guia.activo) "Sí" else "No", if (guia.activo) "Yes" else "No"))
                FilaDetalle(textoSouthbound("Servicios asignados", "Assigned services"), guia.totalServicios?.toString() ?: "—")
            }
        }
        item {
            OutlinedButton(onClick = { vm.detalleGuia(guia.id) }, enabled = !estado.cargando) {
                Text(textoSouthbound("Actualizar", "Refresh"))
            }
        }
    }
}

@Composable
private fun DetalleTransportistaContenido(transportista: TransportistaSouthbound, vm: SouthboundViewModel) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { VolverDetalle { vm.cerrarDetalle() } }
        estadoGeneral(estado) { vm.detalleTransportista(transportista.id) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(transportista.nombre, style = MaterialTheme.typography.headlineSmall)
                FilaDetalle(textoSouthbound("Código", "Code"), transportista.codigo)
                FilaDetalle(textoSouthbound("Oficina", "Office"), transportista.oficina.nombre)
                FilaDetalle(textoSouthbound("Evaluación", "Rating"), "%.2f".format(transportista.evaluacionPromedio))
                FilaDetalle(textoSouthbound("Fecha de ingreso", "Hire date"), transportista.fechaIngreso)
                FilaDetalle(
                    textoSouthbound("Activo", "Active"),
                    textoSouthbound(if (transportista.activo) "Sí" else "No", if (transportista.activo) "Yes" else "No")
                )
                FilaDetalle(
                    textoSouthbound("Servicios asignados", "Assigned services"),
                    transportista.totalServicios?.toString() ?: "—"
                )
            }
        }
        item {
            Text(textoSouthbound("Flota", "Fleet"), style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (transportista.vehiculos.isEmpty()) {
                    Text("—")
                } else {
                    transportista.vehiculos.forEach { vehiculo ->
                        FilaDetalle(vehiculo.patente, "${vehiculo.tipo} · ${vehiculo.capacidad} pax")
                    }
                }
            }
        }
        item {
            OutlinedButton(onClick = { vm.detalleTransportista(transportista.id) }, enabled = !estado.cargando) {
                Text(textoSouthbound("Actualizar", "Refresh"))
            }
        }
    }
}

@Composable
private fun DetalleClienteContenido(cliente: ClienteApiSouthbound, vm: SouthboundViewModel) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { VolverDetalle { vm.cerrarDetalle() } }
        estadoGeneral(estado) { vm.detalleCliente(cliente.id) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(cliente.nombre, style = MaterialTheme.typography.headlineSmall)
                FilaDetalle(textoSouthbound("Código", "Code"), cliente.codigo)
                FilaDetalle(textoSouthbound("País", "Country"), cliente.pais)
                FilaDetalle(textoSouthbound("Total servicios", "Total services"), cliente.totalServicios?.toString() ?: "—")
                FilaDetalle(textoSouthbound("Total pasajeros", "Total passengers"), cliente.totalPax?.toString() ?: "—")
            }
        }
        item {
            OutlinedButton(onClick = { vm.detalleCliente(cliente.id) }, enabled = !estado.cargando) {
                Text(textoSouthbound("Actualizar", "Refresh"))
            }
        }
    }
}

private fun LazyListScope.estadoGeneral(estado: SouthboundUiState, onReintentar: () -> Unit) {
    if (estado.cargando) {
        item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
    }
    if (estado.esCopia) {
        item {
            Text(
                textoSouthbound(
                    "No se pudo actualizar. Mostrando copia guardada: ",
                    "Unable to refresh. Showing saved copy: "
                ) + FORMATO_SELLO_CORTO.format(Date(estado.guardadoEn)),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
    estado.error?.let { codigo ->
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(mensajeSouthbound(codigo), color = MaterialTheme.colorScheme.error)
                estado.mensajeError?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                estado.requestId?.let {
                    Text("requestId: $it", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onReintentar) {
                    Text(textoSouthbound("Reintentar", "Retry"))
                }
            }
        }
    }
}

@Composable
private fun AvisoDatos() {
    Text(
        textoSouthbound(
            "Datos ficticios de Southbound · Solo consulta. Las acciones de la app se guardan por separado.",
            "Southbound sample data · Read only. App actions are stored separately."
        ),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun CampoFecha(valor: String, onCambio: (String) -> Unit, etiqueta: String, modifier: Modifier = Modifier) {
    LinceInputField(
        etiqueta = etiqueta,
        valor = valor,
        enCambioValor = onCambio,
        lugar = "AAAA-MM-DD",
        modifier = modifier
    )
}

@Composable
private fun FilaChips(
    etiqueta: String,
    opciones: List<Pair<String, String>>,
    seleccion: String,
    habilitado: Boolean,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            opciones.forEach { (valor, texto) ->
                FilterChip(
                    selected = seleccion == valor,
                    onClick = { onSelect(valor) },
                    enabled = habilitado,
                    label = { Text(texto) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaServicio(servicio: ServicioSouthbound, onClick: () -> Unit) {
    LinceCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(servicio.codigo, style = MaterialTheme.typography.labelMedium)
            Text(servicio.descripcion, style = MaterialTheme.typography.titleMedium)
            Text("${fechaCorta(servicio.fecha)} · ${servicio.horaPickup}")
            Text("${servicio.pickup} → ${servicio.dropoff}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(textoSouthbound("Cliente: ", "Client: ") + servicio.cliente.nombre)
            Text(textoSouthbound("Servicio: ", "Service: ") + estadoServicioTexto(servicio.estadoServicio))
            Text(textoSouthbound("Asignación: ", "Assignment: ") + estadoAsignacionTexto(servicio.statusPL))
            servicio.transportista?.let { Text(textoSouthbound("Transportista: ", "Driver: ") + it.nombre) }
            servicio.guia?.let { Text(textoSouthbound("Guía: ", "Guide: ") + it.nombre) }
        }
    }
}

@Composable
private fun FilaDetalle(etiqueta: String, valor: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(etiqueta + ":", fontWeight = FontWeight.SemiBold)
        Text(valor)
    }
}

@Composable
private fun TotalResultados(total: Int) {
    Text("$total " + textoSouthbound("resultados", "results"))
}

@Composable
private fun SinResultados() {
    Text(textoSouthbound("No hay resultados para esta consulta.", "No results for this query."))
}

@Composable
private fun Paginador(
    pagina: Int,
    paginas: Int,
    habilitado: Boolean,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onAnterior, enabled = habilitado && pagina > 1) {
                Text(textoSouthbound("Anterior", "Previous"))
            }
            OutlinedButton(onClick = onSiguiente, enabled = habilitado && pagina < paginas) {
                Text(textoSouthbound("Siguiente", "Next"))
            }
        }
        Text(textoSouthbound("Página ", "Page ") + "$pagina / ${paginas.coerceAtLeast(1)}")
    }
}

@Composable
private fun Desglose(titulo: String, conteos: List<ConteoSouthbound>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        if (conteos.isEmpty()) {
            Text("—")
        } else {
            conteos.forEach { conteo ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(conteo.clave, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(textoSouthbound("Servicios: ", "Services: ") + conteo.servicios)
                    Text(textoSouthbound("Pax: ", "Pax: ") + conteo.pax)
                }
            }
        }
    }
}

@Composable
private fun VolverDetalle(onVolver: () -> Unit) {
    OutlinedButton(onClick = onVolver) {
        Text(textoSouthbound("Volver a la lista", "Back to list"))
    }
}

@Composable
private fun mensajeSouthbound(codigo: String): String = when (codigo) {
    "configuracion" -> textoSouthbound("Falta configurar la clave de Southbound.", "The Southbound key is not configured.")
    "clave" -> textoSouthbound("La clave no es válida o ya no tiene acceso.", "The key is invalid or no longer has access.")
    "no_encontrado" -> textoSouthbound("El recurso no existe.", "The resource does not exist.")
    "validacion" -> textoSouthbound("Revisa los filtros: usa fechas con formato AAAA-MM-DD y un rango válido.", "Check the filters: use YYYY-MM-DD dates and a valid range.")
    "limite" -> textoSouthbound("Demasiadas consultas. Intenta nuevamente en unos momentos.", "Too many requests. Try again shortly.")
    "servidor" -> textoSouthbound("Southbound no está disponible. Intenta más tarde.", "Southbound is unavailable. Try again later.")
    "conexion" -> textoSouthbound("No se pudo conectar y no hay una copia guardada de esta consulta.", "Unable to connect and no saved copy exists for this query.")
    "formato" -> textoSouthbound("La respuesta de Southbound tiene un formato inesperado.", "Southbound returned an unexpected response format.")
    else -> textoSouthbound("No se pudo cargar la información. Intenta nuevamente.", "Unable to load data. Please try again.")
}
