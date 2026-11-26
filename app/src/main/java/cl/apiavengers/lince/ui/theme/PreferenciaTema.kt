package cl.apiavengers.lince.ui.theme

import android.content.Context
import cl.apiavengers.lince.datos.preferencias.Preferencias
import cl.apiavengers.lince.datos.preferencias.PreferenciasDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

enum class ModoTema(val etiqueta: String) {
    SISTEMA("Sistema"),
    CLARO("Claro"),
    OSCURO("Oscuro")
}

object PreferenciaTema {
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var almacen: PreferenciasDataStore? = null
    private var iniciado = false

    private val _modo = MutableStateFlow(ModoTema.SISTEMA)
    val modo: StateFlow<ModoTema> = _modo.asStateFlow()

    fun iniciar(context: Context) {
        if (iniciado) return
        iniciado = true
        val preferencias = Preferencias.obtener(context)
        almacen = preferencias
        runCatching {
            _modo.value = leer(runBlocking { preferencias.modoTema.first() })
        }
        alcance.launch {
            preferencias.modoTema.collect { valor -> _modo.value = leer(valor) }
        }
    }

    fun fijar(nuevo: ModoTema) {
        _modo.value = nuevo
        almacen?.let { preferencias ->
            alcance.launch { preferencias.fijarModoTema(nuevo.name) }
        }
    }

    private fun leer(valor: String?): ModoTema =
        runCatching { valor?.let(ModoTema::valueOf) }.getOrNull() ?: ModoTema.SISTEMA
}
