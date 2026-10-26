package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.R
import cl.apiavengers.lince.datos.DatosPrueba
import cl.apiavengers.lince.ui.components.BotonPrincipal

@Composable
fun LoginScreen(error: String?, onLogin: (String, String) -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbar.showSnackbar(it) }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var cuenta by rememberSaveable { mutableStateOf("") }
            var clave by remember { mutableStateOf("") }
            var verClave by remember { mutableStateOf(false) }
            Image(painterResource(R.drawable.ic_lince), "Logo Lince", Modifier.fillMaxWidth().height(96.dp))
            Text("Proyecto Lince", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "Servicios claros, respuestas a tiempo",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(8.dp))
            Text("Bienvenido", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Ingresa con tu cuenta de prueba.")
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            OutlinedTextField(
                value = cuenta,
                onValueChange = { cuenta = it },
                label = { Text("Usuario") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Contraseña") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (verClave) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { verClave = !verClave }) {
                        Text(if (verClave) "Ocultar" else "Mostrar", fontSize = 12.sp)
                    }
                }
            )
            BotonPrincipal(
                texto = "Iniciar sesión",
                onClick = { onLogin(cuenta, clave) },
                enabled = cuenta.isNotBlank() && clave.isNotBlank()
            )
            Text(
                "El rol se valida al ingresar. Solo se utilizan datos ficticios.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            HorizontalDivider()
            Text("Cuentas de prueba · clave Lince123", style = MaterialTheme.typography.titleSmall)
            DatosPrueba.usuarios.forEach { usuario ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${usuario.rol.etiqueta}: ${usuario.cuenta}", fontSize = 13.sp)
                }
            }
        }
    }
}
