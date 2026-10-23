package cl.apiavengers.lince.datos

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import cl.apiavengers.lince.datos.local.*
import cl.apiavengers.lince.modelo.EstadoServicio
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ServiciosRoomTest {
    private val contexto get() = RuntimeEnvironment.getApplication()
    private lateinit var db: LinceDatabase
    private fun abrir() = Room.databaseBuilder(contexto, LinceDatabase::class.java, "servicios-test.db")
        .addMigrations(LinceDatabase.MIGRACION_1_2).allowMainThreadQueries().build()
    private fun repositorio() = RepositorioLince(AlmacenServiciosRoom(db.servicioDao()))
    @Before fun preparar() { contexto.deleteDatabase("servicios-test.db"); db = abrir() }
    @After fun limpiar() { db.close(); contexto.deleteDatabase("servicios-test.db") }

    @Test fun confirmaRechazaYRecuperaTrasCerrarLaBase() {
        val repo = repositorio()
        repo.iniciarSesion("conductor.demo", "Lince123")
        repo.responderServicio("LIN-001", true, "")
        repo.responderServicio("LIN-003", false, "Cambio de horario")
        val fechaOriginal = repo.obtenerServicioPorId("LIN-003").fecha
        db.close()
        db = abrir()
        val nuevo = repositorio()
        nuevo.iniciarSesion("conductor.demo", "Lince123")
        assertEquals(EstadoServicio.CONFIRMADA, nuevo.obtenerServicioPorId("LIN-001").estado)
        assertEquals("Cambio de horario", nuevo.obtenerServicioPorId("LIN-003").motivoRechazo)
        assertEquals(fechaOriginal, nuevo.obtenerServicioPorId("LIN-003").fecha)
        assertEquals(4, nuevo.consultarHistorial().size)
        nuevo.iniciarSesion("guia.demo", "Lince123")
        assertFalse(nuevo.consultarHistorial().any { it.id == "LIN-003" })
        nuevo.iniciarSesion("coordinador.demo", "Lince123")
        assertEquals(3, nuevo.resumenCoordinador().pendientes)
        assertEquals(8, nuevo.resumenCoordinador().historialTotal)
        assertEquals(14, db.servicioDao().consultar().size)
    }
    @Test fun daoRechazaRespuestaDuplicadaOAjenas() {
        repositorio().iniciarSesion("conductor.demo", "Lince123")
        val dao = db.servicioDao()
        assertEquals(0, dao.responder("LIN-001", "GUI-002", "CONFIRMADA", null, "2030-01-01T00:00:00Z"))
        assertEquals(1, dao.responder("LIN-001", "DRV-001", "CONFIRMADA", null, "2030-01-01T00:00:00Z"))
        assertEquals(0, dao.responder("LIN-001", "DRV-001", "RECHAZADA", "Otro", "2030-01-01T00:00:00Z"))
    }
    @Test fun migracionConservaAgendaDeVersionUno() {
        db.close()
        contexto.deleteDatabase("servicios-test.db")
        val archivo = contexto.getDatabasePath("servicios-test.db")
        archivo.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(archivo, null).use { anterior ->
            anterior.execSQL("CREATE TABLE disponibilidad (cuenta TEXT NOT NULL, fecha TEXT NOT NULL, PRIMARY KEY(cuenta,fecha))")
            anterior.execSQL("CREATE TABLE agendas_importadas (cuenta TEXT NOT NULL PRIMARY KEY)")
            anterior.execSQL("INSERT INTO disponibilidad VALUES ('conductor.demo','2030-01-10')")
            anterior.execSQL("INSERT INTO agendas_importadas VALUES ('conductor.demo')")
            anterior.version = 1
        }
        db = abrir()
        assertEquals(listOf("2030-01-10"), db.disponibilidadDao().consultar("conductor.demo"))
        assertTrue(db.disponibilidadDao().estaImportada("conductor.demo"))
        repositorio().iniciarSesion("conductor.demo", "Lince123")
        assertEquals(14, db.servicioDao().consultar().size)
    }
}
