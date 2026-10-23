package cl.apiavengers.lince.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import cl.apiavengers.lince.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w411dp-h891dp")
class FlujoAvanceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun esperar(texto: String) {
        compose.waitUntil(15000) { compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }
    }
    @Test fun ingresoRespuestaValidadaAgendaYPerfil() {
        compose.onNodeWithText("Usuario de demostración").performTextInput("conductor.demo")
        compose.onNodeWithText("Contraseña", substring = false).performTextInput("incorrecta")
        compose.onNodeWithText("Ingresar").performScrollTo().performClick()
        esperar("Usuario o contraseña incorrectos.")
        compose.onNodeWithText("Contraseña", substring = false).performTextReplacement("Lince123")
        compose.onNodeWithText("Ingresar").performClick()
        esperar("Ver mis servicios")
        compose.onNodeWithText("Ver mis servicios").performScrollTo().performClick()
        compose.onNodeWithText("transfer de mañana").performScrollTo().performClick()
        compose.onNodeWithText("Rechazar").performScrollTo().performClick()
        esperar("Escribe un motivo de rechazo de entre 1 y 300 caracteres.")
        compose.onNodeWithText("Motivo de rechazo").performScrollTo().performTextInput("No disponible")
        compose.onNodeWithText("Rechazar").performScrollTo().performClick()
        esperar("Rechazo guardado.")
        compose.onNodeWithText("Motivo: No disponible").assertExists()
        compose.onNodeWithText("Rechazar").assertDoesNotExist()
        compose.onNodeWithContentDescription("Volver").performClick()
        compose.onNodeWithText("Agenda", substring = false).performClick()
        esperar("Guardar disponibilidad")
        compose.waitUntil(15000) { compose.onAllNodes(isToggleable() and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(isToggleable()).performClick()
        compose.onNodeWithText("Guardar disponibilidad").performScrollTo().performClick()
        esperar("Disponibilidad guardada en este dispositivo.")
        compose.onNodeWithText("Elegir fecha").performScrollTo().performClick()
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Perfil", substring = false).performClick()
        compose.onNodeWithText("Nombre de demostración").performScrollTo().performTextReplacement("")
        compose.onNodeWithText("Guardar cambios").performScrollTo().performClick()
        esperar("El nombre debe tener entre 2 y 120 caracteres.")
        compose.onNodeWithText("Cerrar sesión").performScrollTo().performClick()
        esperar("Usuario de demostración")
        compose.onNodeWithText("Ingresar").assertExists()
    }
}
