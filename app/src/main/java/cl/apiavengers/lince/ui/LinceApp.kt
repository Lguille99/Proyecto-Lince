package cl.apiavengers.lince.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.apiavengers.lince.ui.screens.LoginScreen
import cl.apiavengers.lince.ui.screens.ServiciosScreen
import cl.apiavengers.lince.viewmodel.LinceViewModel

@Composable
fun LinceApp(modelo: LinceViewModel = viewModel()) {
    val usuario = modelo.usuario
    if (usuario == null) {
        LoginScreen(error = modelo.error, onLogin = modelo::iniciarSesion)
    } else {
        ServiciosScreen(
            usuario = usuario,
            servicios = modelo.servicios,
            onCerrarSesion = modelo::cerrarSesion
        )
    }
}
