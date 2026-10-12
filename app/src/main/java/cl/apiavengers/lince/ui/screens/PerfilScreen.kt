package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.apiavengers.lince.modelo.Usuario
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun PerfilScreen(
    vm: LinceViewModel,
    onCerrarSesion: () -> Unit,
    onNavigateToInicio: () -> Unit = {},
    onNavigateToServicios: () -> Unit = {},
    onNavigateToAgenda: () -> Unit = {}
) {
    val usuario = vm.usuario
    var selectedBottomItem by remember { mutableStateOf(3) } 

    var identificador by remember { mutableStateOf(usuario?.id ?: "") }
    var nombreDemostracao by remember { mutableStateOf(usuario?.nombre ?: "") }
    var correoPrueba by remember { mutableStateOf(usuario?.correo ?: "") }
    
    var mensajeGuardado by remember { mutableStateOf(false) }

    val bottomItems = listOf("Inicio", "Servicios", "Agenda", "Perfil")
    val bottomIcons = listOf(
        Icons.Default.Home,
        Icons.AutoMirrored.Filled.List,
        Icons.Default.DateRange,
        Icons.Default.Person
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                bottomItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(bottomIcons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedBottomItem == index,
                        onClick = {
                            selectedBottomItem = index
                            when (index) {
                                0 -> onNavigateToInicio()
                                1 -> onNavigateToServicios()
                                2 -> onNavigateToAgenda()
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Mi perfil",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Cuenta ficticia · ${usuario?.rol?.etiqueta ?: "Usuario"}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = identificador,
                    onValueChange = { identificador = it },
                    label = { Text("Identificador") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = nombreDemostracao,
                    onValueChange = { nombreDemostracao = it },
                    label = { Text("Nombre de demostración") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = correoPrueba,
                    onValueChange = { correoPrueba = it },
                    label = { Text("Correo de prueba") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = usuario?.rol?.etiqueta ?: "Conductor",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Rol (no editable)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )
            }

            if (mensajeGuardado) {
                item {
                    Text(
                        text = "¡Cambios guardados exitosamente!",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        vm.actualizarUsuario(nombreDemostracao, correoPrueba, identificador)
                        mensajeGuardado = true
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Guardar cambios", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        vm.cerrarSesion()
                        onCerrarSesion()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Cerrar sesión", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            item {
                Text(
                    text = "No ingreses información personal real.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}