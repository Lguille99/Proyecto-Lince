package cl.apiavengers.lince

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.apiavengers.lince.navigation.AppNavigation
import cl.apiavengers.lince.ui.theme.ModoTema
import cl.apiavengers.lince.ui.theme.PreferenciaTema
import cl.apiavengers.lince.ui.theme.TemaLince

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenciaTema.iniciar(this)
        val sistemaOscuro = (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val splashOscuro = when (PreferenciaTema.modo.value) {
            ModoTema.SISTEMA -> sistemaOscuro
            ModoTema.CLARO -> false
            ModoTema.OSCURO -> true
        }
        setTheme(if (splashOscuro) R.style.Theme_Lince_Starting_Oscuro else R.style.Theme_Lince_Starting)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val modo by PreferenciaTema.modo.collectAsStateWithLifecycle()
            val oscuro = when (modo) {
                ModoTema.SISTEMA -> isSystemInDarkTheme()
                ModoTema.CLARO -> false
                ModoTema.OSCURO -> true
            }
            TemaLince(darkTheme = oscuro) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
