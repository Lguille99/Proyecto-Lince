package cl.apiavengers.lince.datos.preferencias

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.linceDataStore by preferencesDataStore(name = "lince_preferencias")

class PreferenciasDataStore(context: Context) {
    private val datos = context.applicationContext.linceDataStore

    val modoTema: Flow<String?> = datos.data.map { it[CLAVE_MODO_TEMA] }
    val idioma: Flow<String?> = datos.data.map { it[CLAVE_IDIOMA] }
    val ultimaSincronizacion: Flow<String?> = datos.data.map { it[CLAVE_ULTIMA_SINCRONIZACION] }

    suspend fun fijarModoTema(valor: String) {
        datos.edit { it[CLAVE_MODO_TEMA] = valor }
    }

    suspend fun fijarIdioma(valor: String) {
        datos.edit { it[CLAVE_IDIOMA] = valor }
    }

    suspend fun fijarUltimaSincronizacion(valor: String) {
        datos.edit { it[CLAVE_ULTIMA_SINCRONIZACION] = valor }
    }

    private companion object {
        val CLAVE_MODO_TEMA = stringPreferencesKey("modo_tema")
        val CLAVE_IDIOMA = stringPreferencesKey("idioma")
        val CLAVE_ULTIMA_SINCRONIZACION = stringPreferencesKey("ultima_sincronizacion")
    }
}

object Preferencias {
    @Volatile private var instancia: PreferenciasDataStore? = null

    fun obtener(context: Context): PreferenciasDataStore = instancia ?: synchronized(this) {
        instancia ?: PreferenciasDataStore(context.applicationContext).also { instancia = it }
    }
}
