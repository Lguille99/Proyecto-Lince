@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.ui.components.*
import cl.apiavengers.lince.ui.montoClp
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun PerfilScreen(
    vm: LinceViewModel,
    onCerrarSesion: () -> Unit,
    onNavigateToInicio: () -> Unit = {},
    onNavigateToServicios: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {},
    onNavigateToEquipo: () -> Unit = {}
) {
    val usuario = vm.usuario
    val snackbar = remember { SnackbarHostState() }
    var nombre by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.nombre.orEmpty()) }
    var correo by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.correo.orEmpty()) }
    var telefono by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.telefono.orEmpty()) }
    var zona by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.zona.orEmpty()) }
    var idiomas by remember(usuario?.id) { mutableStateOf(usuario?.idiomas ?: listOf(Idioma.ESPANOL)) }

    val mensaje = vm.mensaje
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            snackbar.showSnackbar(mensaje)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            BarraSuperior(
                titulo = "Mi perfil",
                acciones = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        bottomBar = {
            BarraInferior(
                esCoordinador = vm.esCoordinador(),
                seleccion = "Perfil",
                onInicio = onNavigateToInicio,
                onServicios = onNavigateToServicios,
                onAgenda = onNavigateToAgenda,
                onEquipo = onNavigateToEquipo,
                onPerfil = {}
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
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(usuario?.nombre.orEmpty(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    Text(
                        usuario?.rol?.etiqueta.orEmpty(),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    if (usuario != null && !vm.esCoordinador()) {
                        Text(
                            "Acumulado: ${montoClp(vm.totalGanado(usuario.id))}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        vm.promedioCalificacion(usuario.id)?.let {
                            Text(
                                "Calificación promedio: ${"%.1f".format(it)}/5",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = usuario?.id.orEmpty(),
                onValueChange = {},
                label = { Text("ID de usuario (solo lectura)") },
                enabled = false,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = zona,
                onValueChange = { zona = it },
                label = { Text("Zona de trabajo") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Idiomas que dominas", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Idioma.values().forEach { idioma ->
                    FilterChip(
                        selected = idioma in idiomas,
                        onClick = {
                            idiomas = if (idioma in idiomas) {
                                if (idiomas.size == 1) idiomas else idiomas - idioma
                            } else {
                                idiomas + idioma
                            }
                        },
                        label = { Text(idioma.etiqueta) }
                    )
                }
            }

            BotonPrincipal(
                texto = "Guardar cambios",
                onClick = { vm.guardarPerfil(nombre, correo, telefono, zona, idiomas) }
            )
            BotonSecundario(texto = "Ver equipo", onClick = onNavigateToEquipo)

            Text(
                if (vm.modoRemoto) {
                    "Los datos se guardan en Supabase. La cuenta y el rol no se pueden modificar."
                } else {
                    "Los datos se guardan en este dispositivo. La cuenta y el rol no se pueden modificar."
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") }
        }
    }
}
