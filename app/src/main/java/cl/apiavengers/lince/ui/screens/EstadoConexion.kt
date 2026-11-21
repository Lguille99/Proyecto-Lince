package cl.apiavengers.lince.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.apiavengers.lince.ui.idioma.textos
import cl.apiavengers.lince.viewmodel.ConexionViewModel

@Composable
fun EstadoConexion(
    modoRemoto: Boolean = false,
    vm: ConexionViewModel = viewModel()
) {
    val conectado by vm.conectado.collectAsStateWithLifecycle()
    val t = textos()
    Surface(color = if (conectado) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer) {
        Text(
            when {
                !conectado -> t.connectionOffline
                modoRemoto -> t.connectionRemote
                else -> t.connectionLocal
            },
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
