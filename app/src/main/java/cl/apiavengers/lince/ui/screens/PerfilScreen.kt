@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
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
import cl.apiavengers.lince.ui.theme.Colores
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

    val esCoord = vm.esCoordinador()
    val itemsBarra = if (esCoord) {
        listOf(
            Icons.Default.Home to t.navHome,
            Icons.AutoMirrored.Filled.List to t.navServices,
            Icons.Default.Person to t.navTeam,
            Icons.Default.Face to t.navProfile
        )
    } else {
        listOf(
            Icons.Default.Home to t.navHome,
            Icons.AutoMirrored.Filled.List to t.navServices,
            Icons.Default.DateRange to t.navAgenda,
            Icons.Default.Face to t.navProfile
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            LinceTopBar(
                titulo = t.profileTitle,
                accionDerecha = onCerrarSesion,
                iconoDerecha = Icons.AutoMirrored.Filled.ExitToApp
            )
        },
        bottomBar = {
            LinceFloatingBottomBar(
                items = itemsBarra,
                indiceSeleccionado = 3,
                enSeleccionar = { indice ->
                    when (indice) {
                        0 -> onNavigateToInicio()
                        1 -> onNavigateToServicios()
                        2 -> if (esCoord) onNavigateToEquipo() else onNavigateToAgenda()
                    }
                },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LinceCard(
                fondo = Colores.topBar,
                forma = RoundedCornerShape(18.dp),
                borde = null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(usuario?.nombre.orEmpty(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Colores.textoBlanco)
                    Text(
                        usuario?.rol?.let { t.rol(it) }.orEmpty(),
                        fontSize = 13.sp,
                        color = Colores.subtituloSobreNavy
                    )
                    if (usuario != null && !esCoord) {
                        Text(
                            t.accumulated(montoClp(vm.totalGanado(usuario.id))),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Colores.textoBlanco
                        )
                        vm.promedioCalificacion(usuario.id)?.let {
                            Text(
                                t.averageRating("%.1f".format(it)),
                                fontSize = 13.sp,
                                color = Colores.subtituloSobreNavy
                            )
                        }
                    }
                }
            }

            LinceInputField(
                etiqueta = t.profileId,
                valor = usuario?.id.orEmpty(),
                enCambioValor = {},
                enabled = false
            )
            LinceInputField(
                etiqueta = t.profileName,
                valor = nombre,
                enCambioValor = { nombre = it },
                error = mostrarErrores && nombreError != null,
                mensajeError = nombreError
            )
            LinceInputField(
                etiqueta = t.profileEmail,
                valor = correo,
                enCambioValor = { correo = it },
                teclado = KeyboardOptions(keyboardType = KeyboardType.Email),
                error = mostrarErrores && correoError != null,
                mensajeError = correoError
            )
            LinceInputField(
                etiqueta = t.profilePhone,
                valor = telefono,
                enCambioValor = { telefono = it },
                teclado = KeyboardOptions(keyboardType = KeyboardType.Phone),
                error = mostrarErrores && telefonoError != null,
                mensajeError = telefonoError
            )
            LinceInputField(
                etiqueta = t.profileZone,
                valor = zona,
                enCambioValor = { zona = it },
                error = mostrarErrores && zonaError != null,
                mensajeError = zonaError
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
                t.profileDataLocal,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onCerrarSesion) { Text(t.logout) }
        }
    }
}
