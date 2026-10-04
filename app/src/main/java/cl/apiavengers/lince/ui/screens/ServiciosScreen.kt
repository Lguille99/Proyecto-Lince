package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.apiavengers.lince.datos.DatosPrueba
import cl.apiavengers.lince.modelo.*

@Composable
fun ServiciosScreen(usuario: Usuario, servicios: List<Servicio>, onCerrarSesion: () -> Unit) {
    Scaffold { margen ->
        LazyColumn(Modifier.fillMaxSize().padding(margen).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Proyecto Lince", style = MaterialTheme.typography.headlineMedium)
                Text("${usuario.nombre} · ${usuario.rol.etiqueta}")
                TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") }
                Text(if (usuario.rol == Rol.COORDINADOR) "Todos los servicios" else "Mis servicios",
                    style = MaterialTheme.typography.titleLarge)
                Text("Datos de prueba · solo consulta", style = MaterialTheme.typography.bodySmall)
            }
            items(servicios, key = { it.id }) { servicio ->
                TarjetaServicio(servicio, usuario.rol == Rol.COORDINADOR)
            }
        }
    }
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
