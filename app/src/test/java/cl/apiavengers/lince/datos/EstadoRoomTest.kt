package cl.apiavengers.lince.datos

import androidx.room.Room
import cl.apiavengers.lince.datos.local.AlmacenEstadoRoom
import cl.apiavengers.lince.datos.local.LinceDatabase
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EstadoRoomTest {
    private val contexto get() = RuntimeEnvironment.getApplication()
    private lateinit var db: LinceDatabase
    private val servicio = CambioServicio(
        estado = EstadoServicio.CONFIRMADA,
        asignadoA = "guia-1",
        acompananteId = "guia-2",
        observaciones = "Listo",
        puntaje = 5,
        calificadoPor = "coordinador"
    )
    private val perfil = CambioPerfil(
        nombre = "Ana Pérez",
        correo = "ana@lince.cl",
        telefono = "91234567",
        zona = "Centro",
        idiomas = listOf(Idioma.ESPANOL, Idioma.INGLES)
    )

    private fun abrir(): LinceDatabase = Room.databaseBuilder(
        contexto, LinceDatabase::class.java, "prueba-estado.db"
    ).allowMainThreadQueries().build()

    private fun almacen(): AlmacenEstadoRoom = AlmacenEstadoRoom(db.estadoDao())

    @Before fun preparar() {
        contexto.deleteDatabase("prueba-estado.db")
        db = abrir()
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase("prueba-estado.db")
    }

    @Test fun conservaServiciosYPerfilesAlReabrir() {
        val original = EstadoGuardado(
            servicios = mapOf("servicio-1" to servicio),
            perfiles = mapOf("guia-1" to perfil)
        )
        almacen().guardar(original)
        db.close()
        db = abrir()
        assertEquals(original, almacen().leer())
    }

    @Test fun importaLegadoUnaVezYDejaDeConsultarlo() {
        val legado = object : AlmacenEstado {
            var lecturas = 0
            override fun leer(): EstadoGuardado {
                lecturas++
                return EstadoGuardado(
                    servicios = mapOf("servicio-1" to servicio),
                    perfiles = mapOf("guia-1" to perfil)
                )
            }
            override fun guardar(estado: EstadoGuardado) = Unit
        }
        val almacen = AlmacenEstadoRoom(db.estadoDao(), legado)
        val primera = almacen.leer()
        assertEquals(1, legado.lecturas)
        assertEquals(servicio, primera.servicios["servicio-1"])
        assertEquals(perfil, primera.perfiles["guia-1"])
        val segunda = almacen.leer()
        assertEquals(1, legado.lecturas)
        assertEquals(primera, segunda)
    }

    @Test fun reemplazaElEstadoSinDuplicarFilas() {
        almacen().guardar(EstadoGuardado(mapOf("servicio-1" to servicio), mapOf("guia-1" to perfil)))
        almacen().guardar(EstadoGuardado(mapOf("servicio-2" to servicio), mapOf("guia-2" to perfil)))
        val guardado = almacen().leer()
        assertEquals(setOf("servicio-2"), guardado.servicios.keys)
        assertEquals(setOf("guia-2"), guardado.perfiles.keys)
    }
}
