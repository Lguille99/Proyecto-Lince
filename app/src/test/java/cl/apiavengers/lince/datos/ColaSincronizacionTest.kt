package cl.apiavengers.lince.datos

import androidx.room.Room
import cl.apiavengers.lince.datos.local.ColaSincronizacion
import cl.apiavengers.lince.datos.local.LinceDatabase
import cl.apiavengers.lince.datos.local.OperacionPendiente
import cl.apiavengers.lince.datos.local.OperacionPendienteEntity
import cl.apiavengers.lince.datos.local.Sincronizador
import cl.apiavengers.lince.datos.local.SincronizadorLocal
import cl.apiavengers.lince.datos.local.TipoOperacionSync
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ColaSincronizacionTest {
    private lateinit var db: LinceDatabase
    private lateinit var cola: ColaSincronizacion

    @Before fun preparar() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            LinceDatabase::class.java
        ).allowMainThreadQueries().build()
        cola = ColaSincronizacion(db.operacionPendienteDao())
    }

    @After fun limpiar() {
        db.close()
    }

    @Test fun encolaYEnviaPendientesAlRecuperarConectividad() = runBlocking {
        cola.encolar(OperacionPendiente(TipoOperacionSync.CONFIRMAR, "LIN-001", "Confirmar"))
        cola.encolar(OperacionPendiente(TipoOperacionSync.CHECK_IN, "LIN-002", "Check-in"))
        assertEquals(2, cola.contar())

        val enviadas = cola.sincronizar(SincronizadorLocal(pausaMs = 0))

        assertEquals(2, enviadas)
        assertEquals(0, cola.contar())
        cola.limpiarSincronizadas()
        assertTrue(cola.pendientes().isEmpty())
    }

    @Test fun mantieneLosRegistrosParaReintentarSiFallaElEnvio() = runBlocking {
        cola.encolar(OperacionPendiente(TipoOperacionSync.CALIFICAR, "LIN-003", "Calificar"))
        val fallido = object : Sincronizador {
            override suspend fun enviar(operacion: OperacionPendienteEntity): Boolean = false
        }

        assertEquals(0, cola.sincronizar(fallido))
        assertEquals(0, cola.contar())

        cola.reintentarErrores()

        assertEquals(1, cola.contar())
        assertEquals(1, cola.sincronizar(SincronizadorLocal(pausaMs = 0)))
        assertEquals(0, cola.contar())
    }
}
