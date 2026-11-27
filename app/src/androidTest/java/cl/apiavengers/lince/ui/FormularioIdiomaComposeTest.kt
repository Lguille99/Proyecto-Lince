package cl.apiavengers.lince.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.apiavengers.lince.ui.idioma.TextosEspanol
import cl.apiavengers.lince.ui.idioma.TextosIngles
import cl.apiavengers.lince.ui.theme.LinceTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormularioIdiomaComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muestraTextosEnEspanol() {
        composeRule.setContent {
            LinceTheme {
                androidx.compose.material3.Text(TextosEspanol.loginButton)
            }
        }
        composeRule.onNodeWithText(TextosEspanol.loginButton).assertIsDisplayed()
    }

    @Test
    fun muestraTextosEnIngles() {
        composeRule.setContent {
            LinceTheme {
                androidx.compose.material3.Text(TextosIngles.loginButton)
            }
        }
        composeRule.onNodeWithText(TextosIngles.loginButton).assertIsDisplayed()
    }
}
