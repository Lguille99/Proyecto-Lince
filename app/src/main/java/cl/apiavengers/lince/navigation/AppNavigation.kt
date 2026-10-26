package cl.apiavengers.lince.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.apiavengers.lince.ui.screens.DisponibilidadScreen
import cl.apiavengers.lince.ui.screens.EjecucionScreen
import cl.apiavengers.lince.ui.screens.EquipoScreen
import cl.apiavengers.lince.ui.screens.HistorialScreen
import cl.apiavengers.lince.ui.screens.InicioUserScreen
import cl.apiavengers.lince.ui.screens.LoginScreen
import cl.apiavengers.lince.ui.screens.PerfilScreen
import cl.apiavengers.lince.ui.screens.PersonaDetalleScreen
import cl.apiavengers.lince.ui.screens.ResumenCoordinacionScreen
import cl.apiavengers.lince.ui.screens.ServicioDetalleScreen
import cl.apiavengers.lince.ui.screens.ServiciosScreen
import cl.apiavengers.lince.viewmodel.DisponibilidadViewModel
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun AppNavigation(vm: LinceViewModel = viewModel()) {
    val navController = rememberNavController()
    val rutaInicio = if (vm.esCoordinador()) "resumen" else "inicio_user"

    val irInicio: () -> Unit = {
        navController.navigate(rutaInicio) {
            launchSingleTop = true
        }
    }
    val irServicios: () -> Unit = {
        navController.navigate("servicios") { launchSingleTop = true }
    }
    val irAgenda: () -> Unit = {
        navController.navigate("agenda") { launchSingleTop = true }
    }
    val irPerfil: () -> Unit = {
        navController.navigate("perfil") { launchSingleTop = true }
    }
    val irEquipo: () -> Unit = {
        navController.navigate("equipo") { launchSingleTop = true }
    }
    val cerrarSesion: () -> Unit = {
        vm.cerrarSesion()
        navController.navigate("login") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                error = vm.error,
                onLogin = { cuenta, clave ->
                    vm.iniciarSesion(cuenta, clave)
                    if (vm.usuario != null) {
                        val destino = if (vm.esCoordinador()) "resumen" else "inicio_user"
                        navController.navigate(destino) {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable("resumen") {
            if (vm.usuario == null || !vm.esCoordinador()) {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            } else {
                ResumenCoordinacionScreen(
                    vm = vm,
                    onServicios = irServicios,
                    onHistorial = { navController.navigate("historial") { launchSingleTop = true } },
                    onEquipo = { navController.navigate("equipo") { launchSingleTop = true } },
                    onCerrarSesion = cerrarSesion
                )
            }
        }

        composable("inicio_user") {
            if (vm.usuario == null) {
                LaunchedEffect(Unit) { navController.navigate("login") }
            } else {
                InicioUserScreen(
                    vm = vm,
                    onNavigateToServicios = irServicios,
                    onNavigateToAgenda = irAgenda,
                    onNavigateToEquipo = { navController.navigate("equipo") { launchSingleTop = true } },
                    onNavigateToPerfil = irPerfil,
                    onNavigateToHistorial = { navController.navigate("historial") { launchSingleTop = true } },
                    onServicioClick = { id -> navController.navigate("servicio_detalle/$id") }
                )
            }
        }

        composable("historial") {
            if (vm.usuario == null) {
                LaunchedEffect(Unit) { navController.navigate("login") }
            } else {
                HistorialScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onServicioClick = { id -> navController.navigate("servicio_detalle/$id") },
                    onNavigateToInicio = irInicio,
                    onNavigateToServicios = irServicios,
                    onNavigateToAgenda = irAgenda,
                    onNavigateToEquipo = irEquipo,
                    onNavigateToPerfil = irPerfil
                )
            }
        }

        composable("servicios") {
            if (vm.usuario == null) {
                LaunchedEffect(Unit) { navController.navigate("login") }
            } else {
                ServiciosScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onServicioClick = { id -> navController.navigate("servicio_detalle/$id") },
                    onEquipoClick = { navController.navigate("equipo") { launchSingleTop = true } },
                    onNavigateToInicio = irInicio,
                    onNavigateToAgenda = irAgenda,
                    onNavigateToPerfil = irPerfil
                )
            }
        }

        composable("equipo") {
            if (vm.usuario == null) {
                LaunchedEffect(Unit) { navController.navigate("login") }
            } else {
                EquipoScreen(
                    vm = vm,
                    onBack = { navController.popBackStack() },
                    onPersonaClick = { id -> navController.navigate("persona_detalle/$id") },
                    onNavigateToInicio = irInicio,
                    onNavigateToServicios = irServicios,
                    onNavigateToAgenda = irAgenda,
                    onNavigateToPerfil = irPerfil
                )
            }
        }

        composable(
            "persona_detalle/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStack ->
            val userId = backStack.arguments?.getString("userId").orEmpty()
            PersonaDetalleScreen(
                vm = vm,
                personaId = userId,
                onBack = { navController.popBackStack() },
                onServicioClick = { sid -> navController.navigate("servicio_detalle/$sid") }
            )
        }

        composable(
            "servicio_detalle/{servicioId}",
            arguments = listOf(navArgument("servicioId") { type = NavType.StringType })
        ) { backStack ->
            val servicioId = backStack.arguments?.getString("servicioId").orEmpty()
            ServicioDetalleScreen(
                vm = vm,
                servicioId = servicioId,
                onBack = { navController.popBackStack() },
                onPersonaClick = { uid -> navController.navigate("persona_detalle/$uid") },
                onIniciar = { id -> navController.navigate("ejecucion/$id") },
                onNavigateToInicio = irInicio,
                onNavigateToServicios = irServicios,
                onNavigateToAgenda = irAgenda,
                onNavigateToEquipo = irEquipo,
                onNavigateToPerfil = irPerfil
            )
        }

        composable(
            "ejecucion/{servicioId}",
            arguments = listOf(navArgument("servicioId") { type = NavType.StringType })
        ) { backStack ->
            val servicioId = backStack.arguments?.getString("servicioId").orEmpty()
            EjecucionScreen(
                vm = vm,
                servicioId = servicioId,
                onBack = { navController.popBackStack() },
                onPersonaClick = { uid -> navController.navigate("persona_detalle/$uid") },
                onNavigateToInicio = irInicio,
                onNavigateToServicios = irServicios,
                onNavigateToAgenda = irAgenda,
                onNavigateToEquipo = irEquipo,
                onNavigateToPerfil = irPerfil
            )
        }

        composable("perfil") {
            if (vm.usuario == null) {
                LaunchedEffect(Unit) { navController.navigate("login") }
            } else {
                PerfilScreen(
                    vm = vm,
                    onCerrarSesion = cerrarSesion,
                    onNavigateToInicio = irInicio,
                    onNavigateToServicios = irServicios,
                    onNavigateToAgenda = irAgenda,
                    onNavigateToEquipo = { navController.navigate("equipo") { launchSingleTop = true } }
                )
            }
        }

        composable("agenda") {
            val usuario = vm.usuario
            if (usuario != null) {
                val disponibilidadVm: DisponibilidadViewModel = viewModel()
                LaunchedEffect(usuario.id) { disponibilidadVm.cargar(usuario) }
                val asignados = remember(usuario.id, vm.servicios) {
                    vm.serviciosDe(usuario.id)
                }
                DisponibilidadScreen(
                    vm = disponibilidadVm,
                    usuario = usuario,
                    serviciosAsignados = asignados,
                    onBack = { navController.popBackStack() },
                    onCerrarSesion = cerrarSesion,
                    onNavigateToInicio = irInicio,
                    onNavigateToServicios = irServicios,
                    onNavigateToPerfil = irPerfil
                )
            } else {
                LaunchedEffect(Unit) { navController.navigate("login") }
            }
        }
    }
}
