package cl.apiavengers.lince.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.apiavengers.lince.R
import cl.apiavengers.lince.datos.DatosPrueba
import cl.apiavengers.lince.modelo.*
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun LinceApp(modelo: LinceViewModel = viewModel()) {
    Scaffold { margen ->
        val usuario = modelo.usuario
        if (usuario == null) {
            Column(Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Ingreso(modelo.error, modelo::iniciarSesion)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(margen).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Text("Proyecto Lince", style = MaterialTheme.typography.headlineMedium)
                    Text("${usuario.nombre} · ${usuario.rol.etiqueta}")
                    TextButton(onClick = modelo::cerrarSesion) { Text("Cerrar sesión") }
                    Text(if (usuario.rol == Rol.COORDINADOR) "Todos los servicios" else "Mis servicios",
                        style = MaterialTheme.typography.titleLarge)
                    Text("Datos de prueba · solo consulta", style = MaterialTheme.typography.bodySmall)
                }
                items(modelo.servicios, key = { it.id }) { servicio ->
                    TarjetaServicio(servicio, usuario.rol == Rol.COORDINADOR)
                }
            }
        }
    }
}

@Composable
private fun Ingreso(error: String?, ingresar: (String, String) -> Unit) {
    var cuenta by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    Image(painterResource(R.drawable.ic_lince), "Logo Lince", Modifier.fillMaxWidth().height(100.dp))
    Text("Proyecto Lince", style = MaterialTheme.typography.headlineLarge)
    Text("Ingresa para consultar tus servicios.")
    OutlinedTextField(cuenta, { cuenta = it }, label = { Text("Usuario") },
        singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(clave, { clave = it }, label = { Text("Contraseña") },
        singleLine = true, modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
    if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
    Button(onClick = { ingresar(cuenta, clave) }, modifier = Modifier.fillMaxWidth(),
        enabled = cuenta.isNotBlank() && clave.isNotBlank()) { Text("Ingresar") }
    Text("Cuentas de prueba", style = MaterialTheme.typography.titleMedium)
    Text("Contraseña: Lince123")
    DatosPrueba.usuarios.forEach { Text("${it.rol.etiqueta}: ${it.cuenta}") }
}

@Composable
private fun TarjetaServicio(servicio: Servicio, mostrarRecurso: Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(servicio.titulo, style = MaterialTheme.typography.titleMedium)
            Text("${servicio.id} · ${servicio.tipo}")
            Text("Fecha: ${servicio.fecha} · ${servicio.hora}")
            Text("Recogida: ${servicio.recogida}")
            Text("Destino: ${servicio.destino}")
            if (mostrarRecurso) {
                val nombre = DatosPrueba.usuarios.find { it.id == servicio.asignadoA }?.nombre
                Text("Asignado a: ${nombre ?: servicio.asignadoA}")
            }
        }
    }
}
