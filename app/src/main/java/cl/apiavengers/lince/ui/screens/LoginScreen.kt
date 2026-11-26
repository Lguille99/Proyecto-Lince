package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.apiavengers.lince.R
import cl.apiavengers.lince.datos.DatosPrueba
import cl.apiavengers.lince.ui.components.*
import cl.apiavengers.lince.ui.idioma.IdiomaApp
import cl.apiavengers.lince.ui.idioma.PreferenciaIdioma
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.ui.theme.Colores

@Composable
fun LoginScreen(
    error: String?,
    cargando: Boolean = false,
    onLogin: (String, String) -> Unit
) {
    var cuenta by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var verClave by remember { mutableStateOf(false) }
    val t = textos()
    val idioma by PreferenciaIdioma.idioma.collectAsStateWithLifecycle()

    Scaffold { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    t.language,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterChip(
                    selected = idioma == IdiomaApp.ESPANOL,
                    onClick = { PreferenciaIdioma.fijar(IdiomaApp.ESPANOL) },
                    label = { Text(t.languageSpanish) }
                )
                FilterChip(
                    selected = idioma == IdiomaApp.INGLES,
                    onClick = { PreferenciaIdioma.fijar(IdiomaApp.INGLES) },
                    label = { Text(t.languageEnglish) }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(84.dp)
                ) {
                    Image(
                        painterResource(R.drawable.ic_lince),
                        t.loginLogoDescription,
                        Modifier.fillMaxSize().padding(12.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        t.appName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        t.appTagline,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(t.loginWelcome, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(t.loginSubtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (error != null) {
                LinceCard(
                    fondo = Colores.errorFondo,
                    borde = null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Colores.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            error,
                            color = Colores.error,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            LinceCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LinceInputField(
                        etiqueta = t.loginUser,
                        valor = cuenta,
                        enCambioValor = { cuenta = it },
                        teclado = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    LinceInputField(
                        etiqueta = t.loginPassword,
                        valor = clave,
                        enCambioValor = { clave = it },
                        teclado = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        accionesTeclado = KeyboardActions(
                            onDone = {
                                if (cuenta.isNotBlank() && clave.isNotBlank() && !cargando) {
                                    onLogin(cuenta, clave)
                                }
                            }
                        ),
                        transformacionVisual = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingComposable = {
                            IconButton(onClick = { verClave = !verClave }) {
                                Icon(
                                    painter = painterResource(
                                        if (verClave) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                                    ),
                                    contentDescription = if (verClave) t.loginHidePassword else t.loginShowPassword,
                                    tint = Colores.primario
                                )
                            }
                        }
                    )
                    BotonPrincipal(
                        texto = t.loginButton,
                        onClick = { onLogin(cuenta, clave) },
                        enabled = cuenta.isNotBlank() && clave.isNotBlank(),
                        cargando = cargando
                    )
                }
            }

            LinceCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(t.loginDemoTitle, style = MaterialTheme.typography.titleSmall)
                    Text(
                        t.loginDemoHint,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()
                    DatosPrueba.usuarios.forEach { usuario ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                    cuenta = usuario.cuenta
                                    clave = "Lince123"
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${t.rol(usuario.rol)}: ${usuario.cuenta}",
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                t.loginUse,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Text(
                t.loginFooter,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
