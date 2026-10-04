package cl.apiavengers.lince.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.ui.screens.CoordinadorScreen
import cl.apiavengers.lince.ui.screens.LoginScreen
import cl.apiavengers.lince.ui.screens.ServiciosScreen
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun AppNavigation(vm: LinceViewModel = viewModel()) {
    val navController = rememberNavController()

    fun volverAlIngreso() {
        navController.navigate("login") {
            popUpTo(navController.graph.id) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun cerrarSesion() {
        vm.cerrarSesion()
        volverAlIngreso()
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                error = vm.error,
                onLogin = { cuenta, clave ->
                    vm.iniciarSesion(cuenta, clave)
                    val usuario = vm.usuario
                    if (usuario != null) {
                        val destino = when (usuario.rol) {
                            Rol.COORDINADOR -> "coordinador"
                            else -> "servicios"
                        }
                        navController.navigate(destino) {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable("coordinador") {
            val usuario = vm.usuario
            if (usuario != null && usuario.rol == Rol.COORDINADOR) {
                CoordinadorScreen(
                    nombre = usuario.nombre,
                    totalServicios = vm.servicios.size,
                    onServicios = {
                        navController.navigate("servicios") { launchSingleTop = true }
                    },
                    onCerrarSesion = { cerrarSesion() }
                )
            } else {
                LaunchedEffect(Unit) { cerrarSesion() }
            }
        }

        composable("servicios") {
            val usuario = vm.usuario
            if (usuario != null) {
                ServiciosScreen(
                    usuario = usuario,
                    servicios = vm.servicios,
                    onCerrarSesion = { cerrarSesion() }
                )
            } else {
                LaunchedEffect(Unit) { volverAlIngreso() }
            }
        }
    }
}
