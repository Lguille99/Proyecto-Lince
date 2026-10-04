package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CoordinadorScreen(
    nombre: String,
    totalServicios: Int,
    onServicios: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Inicio de coordinación", style = MaterialTheme.typography.headlineMedium)
            Text("Bienvenido, $nombre")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Servicios registrados", style = MaterialTheme.typography.titleMedium)
                    Text("$totalServicios", style = MaterialTheme.typography.headlineLarge)
                }
            }
            Button(onClick = onServicios, modifier = Modifier.fillMaxWidth()) {
                Text("Ver servicios")
            }
            OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}
