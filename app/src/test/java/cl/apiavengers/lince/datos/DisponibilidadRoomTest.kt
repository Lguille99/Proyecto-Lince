package cl.apiavengers.lince.datos

import androidx.room.Room
import cl.apiavengers.lince.datos.local.AlmacenDisponibilidadRoom
import cl.apiavengers.lince.datos.local.LinceDatabase
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DisponibilidadRoomTest {
    private val contexto get() = RuntimeEnvironment.getApplication()
    private lateinit var db: LinceDatabase
    private val fecha = LocalDate.of(2030, 1, 10)
    private val legado = object : AlmacenDisponibilidad {
        override fun leer(cuenta: String) = if (cuenta == "anterior") setOf(fecha) else emptySet()
        override fun guardar(cuenta: String, fechas: Set<LocalDate>) = error("No debe escribir JSON")
    }
    private fun abrir(): LinceDatabase = Room.databaseBuilder(
        contexto, LinceDatabase::class.java, "prueba-room.db"
    ).allowMainThreadQueries().build()
    private fun almacen() = AlmacenDisponibilidadRoom(db.disponibilidadDao(), legado)

    @Before fun preparar() {
        contexto.deleteDatabase("prueba-room.db")
        db = abrir()
    }
    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase("prueba-room.db")
    }
    @Test fun conservaDatosAlReabrirYSeparaCuentas() {
        almacen().guardar("conductor", setOf(fecha))
        almacen().guardar("guia", setOf(fecha.plusDays(1)))
        db.close()
        db = abrir()
        assertEquals(setOf(fecha), almacen().leer("conductor"))
        assertEquals(setOf(fecha.plusDays(1)), almacen().leer("guia"))
        almacen().guardar("conductor", emptySet())
        assertTrue(almacen().leer("conductor").isEmpty())
        assertEquals(setOf(fecha.plusDays(1)), almacen().leer("guia"))
    }
    @Test fun importaJsonUnaVezSinRestaurarDiasDesbloqueados() {
        assertEquals(setOf(fecha), almacen().leer("anterior"))
        almacen().guardar("anterior", emptySet())
        db.close()
        db = abrir()
        assertTrue(almacen().leer("anterior").isEmpty())
    }
    @Test fun guardarRepetidamenteNoDuplicaFechas() {
        repeat(3) { almacen().guardar("conductor", setOf(fecha)) }
        assertEquals(listOf(fecha.toString()), db.disponibilidadDao().consultar("conductor"))
    }
}
