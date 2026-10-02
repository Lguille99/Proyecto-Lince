package cl.apiavengers.lince

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.apiavengers.lince.navigation.AppNavigation
import cl.apiavengers.lince.ui.theme.TemaLince

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TemaLince { AppNavigation() } }
    }
}
