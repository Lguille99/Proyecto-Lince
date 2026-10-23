package cl.apiavengers.lince.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.apiavengers.lince.ui.screens.EquipoScreen
import cl.apiavengers.lince.ui.screens.HistorialScreen
import cl.apiavengers.lince.ui.screens.LoginScreen
import cl.apiavengers.lince.ui.screens.ResumenCoordinacionScreen
import cl.apiavengers.lince.ui.screens.ServiciosScreen
import cl.apiavengers.lince.ui.screens.InicioUserScreen
import cl.apiavengers.lince.ui.screens.DisponibilidadScreen
import cl.apiavengers.lince.ui.screens.PerfilScreen
import cl.apiavengers.lince.ui.screens.PersonaDetalleScreen
import cl.apiavengers.lince.ui.screens.ServicioDetalleScreen
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun AppNavigation(vm: LinceViewModel = viewModel()) {
    val navController = rememberNavController()
    val rutaInicio = if (vm.esCoordinador()) "resumen" else "inicio_user"

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LaunchedEffect(vm.usuario) {
                if (vm.usuario != null) {
                    val destino = if (vm.esCoordinador()) "resumen" else "inicio_user"
                    navController.navigate(destino) {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            LoginScreen(error = vm.error, cargando = vm.cargando, onLogin = vm::iniciarSesion)
        }

        composable("resumen") {
            if (vm.usuario == null || !vm.esCoordinador()) {
                LaunchedEffect(Unit) { navController.navigate("login") }
            } else {
                ResumenCoordinacionScreen(
                    vm = vm,
                    resumen = vm.resumen,
                    onNavigateToServicios = { navController.navigate("servicios") },
                    onNavigateToHistorial = { navController.navigate("historial") },
                    onNavigateToEquipo = { navController.navigate("equipo") },
                    onNavigateToAgenda = { navController.navigate("agenda") { launchSingleTop = true } },
                    onNavigateToPerfil = { navController.navigate("perfil") }
                )
            }
        }

        composable("inicio_user") {
            InicioUserScreen(
                vm = vm,
                onNavigateToServicios = { navController.navigate("servicios") },
                onNavigateToAgenda = { navController.navigate("agenda") { launchSingleTop = true } },
                onNavigateToEquipo = { navController.navigate("equipo") },
                onNavigateToPerfil = { navController.navigate("perfil") },
                onNavigateToHistorial = { navController.navigate("historial") },
                onServicioClick = { id -> navController.navigate("servicio_detalle/$id") }
            )
        }

        composable("historial") {
            if (vm.usuario == null) {
                LaunchedEffect(Unit) { navController.navigate("login") }
            } else {
                HistorialScreen(
                    historial = vm.historial,
                    onBack = { navController.popBackStack() },
                    onServicioClick = { id -> navController.navigate("servicio_detalle/$id") },
                    onNavigateToInicio = { navController.navigate(rutaInicio) { launchSingleTop = true } },
                    onNavigateToServicios = { navController.navigate("servicios") },
                    onNavigateToAgenda = { navController.navigate("agenda") { launchSingleTop = true } },
                    onNavigateToPerfil = { navController.navigate("perfil") }
                )
            }
        }

        composable("servicios") {
            val usuario = vm.usuario
            if (usuario != null) {
                ServiciosScreen(
                    usuario = usuario,
                    servicios = vm.servicios,
                    onNavigateToInicio = { navController.navigate(rutaInicio) { launchSingleTop = true } },
                    onNavigateToAgenda = { navController.navigate("agenda") { launchSingleTop = true } },
                    onNavigateToEquipo = { navController.navigate("equipo") },
                    onNavigateToPerfil = { navController.navigate("perfil") },
                    onServicioClick = { id -> navController.navigate("servicio_detalle/$id") }
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

        composable("equipo") {
            if (vm.usuario == null) {
                LaunchedEffect(Unit) { navController.navigate("login") }
            } else {
                EquipoScreen(
                    equipo = vm.equipo,
                    onPersonaClick = { id -> navController.navigate("persona_detalle/$id") },
                    onNavigateToInicio = { navController.navigate(rutaInicio) { launchSingleTop = true } },
                    onNavigateToServicios = { navController.navigate("servicios") },
                    onNavigateToAgenda = { navController.navigate("agenda") { launchSingleTop = true } },
                    onNavigateToPerfil = { navController.navigate("perfil") }
                )
            }
        }

        composable(
            "persona_detalle/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStack ->
            val userId = backStack.arguments?.getString("userId").orEmpty()
            val persona = vm.obtenerPersona(userId)
            PersonaDetalleScreen(
                persona = persona,
                serviciosAsignados = if (persona != null) vm.serviciosDe(persona.id) else emptyList(),
                onBack = { navController.popBackStack() },
                onServicioClick = { sid -> navController.navigate("servicio_detalle/$sid") }
            )
        }

        composable(
            "servicio_detalle/{servicioId}",
            arguments = listOf(navArgument("servicioId") { type = NavType.StringType })
        ) { backStack ->
            val servicioId = backStack.arguments?.getString("servicioId").orEmpty()
            val servicio = vm.obtenerServicio(servicioId)
            val responsable = servicio?.let { vm.obtenerPersona(it.asignadoA) }
            ServicioDetalleScreen(
                servicio = servicio,
                responsable = responsable,
                onBack = { navController.popBackStack() },
                onResponsableClick = { uid -> navController.navigate("persona_detalle/$uid") }
            )
        }

        composable("perfil") {
            PerfilScreen(
                vm = vm,
                onCerrarSesion = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToInicio = { navController.navigate(rutaInicio) { launchSingleTop = true } },
                onNavigateToServicios = { navController.navigate("servicios") },
                onNavigateToAgenda = { navController.navigate("agenda") { launchSingleTop = true } },
                onNavigateToEquipo = { navController.navigate("equipo") }
            )
        }
        composable("agenda") {
            val usuario = vm.usuario
            if (usuario != null) {
                DisponibilidadScreen(
                    usuario = usuario,
                    onNavigateToInicio = {
                        navController.navigate(rutaInicio) {
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
