package cl.apiavengers.lince.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ModoTema(val etiqueta: String) {
    SISTEMA("Sistema"),
    CLARO("Claro"),
    OSCURO("Oscuro")
}

object PreferenciaTema {
    private const val ARCHIVO = "tema_lince"
    private const val CLAVE = "modo"

    private var prefs: SharedPreferences? = null

    private val _modo = MutableStateFlow(ModoTema.SISTEMA)
    val modo: StateFlow<ModoTema> = _modo.asStateFlow()

    fun iniciar(context: Context) {
        val almacen = context.applicationContext
            .getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        prefs = almacen
        _modo.value = leer(almacen.getString(CLAVE, null))
    }

    fun fijar(nuevo: ModoTema) {
        _modo.value = nuevo
        prefs?.edit { putString(CLAVE, nuevo.name) }
    }

    private fun leer(valor: String?): ModoTema =
        runCatching { valor?.let(ModoTema::valueOf) }.getOrNull() ?: ModoTema.SISTEMA
}
