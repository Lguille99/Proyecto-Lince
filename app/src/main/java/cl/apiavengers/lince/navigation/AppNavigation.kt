package cl.apiavengers.lince.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.apiavengers.lince.ui.screens.LoginScreen
import cl.apiavengers.lince.ui.screens.ServiciosScreen
import cl.apiavengers.lince.ui.screens.InicioUserScreen
import cl.apiavengers.lince.ui.screens.DisponibilidadScreen
import cl.apiavengers.lince.ui.screens.PerfilScreen
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
                        navController.navigate("inicio_user") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable("inicio_user") {
            InicioUserScreen(
                vm = vm,
                onNavigateToServicios = { navController.navigate("servicios") },
                onNavigateToAgenda = { navController.navigate("agenda") { launchSingleTop = true } },
                onNavigateToPerfil = {navController.navigate("perfil")},
                onNavigateToHistorial = {}
    )
}

        composable("servicios") {
            val usuario = vm.usuario
            if (usuario != null) {
                ServiciosScreen(
                    usuario = usuario,
                    servicios = vm.servicios,
                    onNavigateToInicio = { navController.navigate("inicio_user") },
                    onNavigateToAgenda = { navController.navigate("agenda") { launchSingleTop = true } },
                    onNavigateToPerfil = {navController.navigate("perfil")} 
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
        composable("perfil") {
            PerfilScreen(
                vm = vm,
                onCerrarSesion = {
                    navController.navigate("login") {
                        popUpTo("perfil") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToInicio = { navController.navigate("inicio_user") },
                onNavigateToServicios = { navController.navigate("servicios") },
                onNavigateToAgenda = { navController.navigate("agenda") { launchSingleTop = true } }
            )
        }
        composable("agenda") {
            val usuario = vm.usuario
            if (usuario != null) {
                DisponibilidadScreen(
                    usuario = usuario,
                    onNavigateToInicio = {
                        navController.navigate("inicio_user") {
                            popUpTo("inicio_user") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToServicios = {
                        navController.navigate("servicios") {
                            popUpTo("agenda") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPerfil = {
                        navController.navigate("perfil") {
                            popUpTo("agenda") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        }

    }
}
