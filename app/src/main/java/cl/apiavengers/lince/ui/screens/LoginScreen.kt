package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.apiavengers.lince.R

@Composable
fun LoginScreen(error: String?, cargando: Boolean = false, onLogin: (String, String) -> Unit) {
    Scaffold { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var cuenta by rememberSaveable { mutableStateOf("") }
            var clave by remember { mutableStateOf("") }
            Image(painterResource(R.drawable.ic_lince), "Logo Lince", Modifier.fillMaxWidth().height(100.dp))
            Text("Proyecto Lince", style = MaterialTheme.typography.headlineLarge)
            Text("Demostración académica · datos ficticios guardados en este teléfono.")
            OutlinedTextField(cuenta, { cuenta = it }, label = { Text("Usuario de demostración") },
                singleLine = true, enabled = !cargando, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(clave, { clave = it }, label = { Text("Contraseña") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            Text("Usuarios: conductor.demo, guia.demo o coordinador.demo")
            Text("Contraseña de demostración: Lince123")
            if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            Button(onClick = { onLogin(cuenta, clave) }, modifier = Modifier.fillMaxWidth(),
                enabled = !cargando && cuenta.isNotBlank() && clave.isNotBlank()) { Text(if (cargando) "Ingresando…" else "Ingresar") }

        }
    }
}
