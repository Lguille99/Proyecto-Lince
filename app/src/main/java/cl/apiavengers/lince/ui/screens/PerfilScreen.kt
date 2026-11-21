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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.ui.components.*
import cl.apiavengers.lince.ui.idioma.IdiomaApp
import cl.apiavengers.lince.ui.idioma.PreferenciaIdioma
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.ui.montoClp
import cl.apiavengers.lince.ui.theme.ModoTema
import cl.apiavengers.lince.ui.theme.PreferenciaTema
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
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val usuario = estado.usuario
    val modoTema by PreferenciaTema.modo.collectAsStateWithLifecycle()
    val idiomaApp by PreferenciaIdioma.idioma.collectAsStateWithLifecycle()
    val t = textos()
    val snackbar = remember { SnackbarHostState() }
    var nombre by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.nombre.orEmpty()) }
    var correo by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.correo.orEmpty()) }
    var telefono by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.telefono.orEmpty()) }
    var zona by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.zona.orEmpty()) }
    var idiomas by remember(usuario?.id) { mutableStateOf(usuario?.idiomas ?: listOf(Idioma.ESPANOL)) }
    var mostrarErrores by rememberSaveable(usuario?.id) { mutableStateOf(false) }

    val nombreError = when {
        nombre.trim().length !in 2..60 -> t.errorName
        else -> null
    }
    val correoError = if (correo.trim().let { it.contains("@") && it.substringAfter("@").contains(".") }) {
        null
    } else {
        t.errorEmail
    }
    val telefonoError = if (telefono.trim().length >= 8) null else t.errorPhone
    val zonaError = if (zona.trim().isNotEmpty()) null else t.errorZone
    val idiomasError = if (idiomas.isNotEmpty()) null else t.errorLanguages
    val formularioValido = listOf(nombreError, correoError, telefonoError, zonaError, idiomasError).all { it == null }

    val mensaje = estado.mensaje
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
                titulo = t.profileTitle,
                acciones = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = t.logout)
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
                        usuario?.rol?.let { t.rol(it) }.orEmpty(),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    if (usuario != null && !vm.esCoordinador()) {
                        Text(
                            t.accumulated(montoClp(vm.totalGanado(usuario.id))),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        vm.promedioCalificacion(usuario.id)?.let {
                            Text(
                                t.averageRating("%.1f".format(it)),
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
                label = { Text(t.profileId) },
                enabled = false,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text(t.profileName) },
                singleLine = true,
                isError = mostrarErrores && nombreError != null,
                supportingText = {
                    if (mostrarErrores && nombreError != null) Text(nombreError)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text(t.profileEmail) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = mostrarErrores && correoError != null,
                supportingText = {
                    if (mostrarErrores && correoError != null) Text(correoError)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text(t.profilePhone) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = mostrarErrores && telefonoError != null,
                supportingText = {
                    if (mostrarErrores && telefonoError != null) Text(telefonoError)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = zona,
                onValueChange = { zona = it },
                label = { Text(t.profileZone) },
                singleLine = true,
                isError = mostrarErrores && zonaError != null,
                supportingText = {
                    if (mostrarErrores && zonaError != null) Text(zonaError)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(t.profileLanguages, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                        label = { Text(t.idioma(idioma)) }
                    )
                }
            }

            if (mostrarErrores && idiomasError != null) {
                Text(idiomasError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            BotonPrincipal(
                texto = t.profileSave,
                onClick = {
                    mostrarErrores = true
                    if (formularioValido) {
                        vm.guardarPerfil(nombre, correo, telefono, zona, idiomas)
                    }
                }
            )
            BotonSecundario(texto = t.profileViewTeam, onClick = onNavigateToEquipo)

            Text(t.profileLanguage, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = idiomaApp == IdiomaApp.ESPANOL,
                    onClick = { PreferenciaIdioma.fijar(IdiomaApp.ESPANOL) },
                    label = { Text(t.languageSpanish) }
                )
                FilterChip(
                    selected = idiomaApp == IdiomaApp.INGLES,
                    onClick = { PreferenciaIdioma.fijar(IdiomaApp.INGLES) },
                    label = { Text(t.languageEnglish) }
                )
            }
            Text(
                t.profileLanguageHint,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(t.profileAppearance, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModoTema.values().forEach { opcion ->
                    FilterChip(
                        selected = modoTema == opcion,
                        onClick = { PreferenciaTema.fijar(opcion) },
                        label = { Text(t.modoTema(opcion)) }
                    )
                }
            }
            Text(
                t.profileAppearanceHint,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                if (estado.modoRemoto) t.profileDataRemote else t.profileDataLocal,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onCerrarSesion) { Text(t.logout) }
        }
    }
}
