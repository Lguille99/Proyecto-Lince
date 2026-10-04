package cl.apiavengers.lince.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.apiavengers.lince.ui.screens.LoginScreen
import cl.apiavengers.lince.ui.screens.ServiciosScreen
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun AppNavigation(vm: LinceViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                error = vm.error,
                onLogin = { cuenta, clave ->
                    vm.iniciarSesion(cuenta, clave)
                    if (vm.usuario != null) {
                        navController.navigate("servicios") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable("servicios") {
            val usuario = vm.usuario
            if (usuario != null) {
                ServiciosScreen(
                    usuario = usuario,
                    servicios = vm.servicios,
                    onCerrarSesion = {
                        vm.cerrarSesion()
                        navController.navigate("login") {
                            popUpTo("servicios") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo("servicios") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}
