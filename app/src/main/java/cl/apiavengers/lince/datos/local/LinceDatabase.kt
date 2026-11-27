package cl.apiavengers.lince.datos.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DisponibilidadEntity::class,
        AgendaImportadaEntity::class,
        EstadoServicioEntity::class,
        PerfilEntity::class,
        OperacionPendienteEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class LinceDatabase : RoomDatabase() {
    abstract fun estadoDao(): EstadoDao
    abstract fun disponibilidadDao(): DisponibilidadDao
    abstract fun operacionPendienteDao(): OperacionPendienteDao

    companion object {
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS servicios (id TEXT NOT NULL PRIMARY KEY, titulo TEXT NOT NULL, tipo TEXT NOT NULL, fecha TEXT NOT NULL, hora TEXT NOT NULL, recogida TEXT NOT NULL, destino TEXT NOT NULL, asignadoA TEXT NOT NULL, estado TEXT NOT NULL, historico INTEGER NOT NULL, motivoRechazo TEXT, respondidoEn TEXT)")
            }
        }

        val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS estado_servicios (" +
                        "id TEXT NOT NULL PRIMARY KEY, " +
                        "estado TEXT NOT NULL, " +
                        "asignadoA TEXT NOT NULL, " +
                        "acompananteId TEXT, " +
                        "respondidoPor TEXT, " +
                        "respondidoEn TEXT, " +
                        "motivoRechazo TEXT, " +
                        "inicioEn TEXT, " +
                        "terminoEn TEXT, " +
                        "inicioPor TEXT, " +
                        "ubicacionInicio TEXT, " +
                        "observaciones TEXT NOT NULL, " +
                        "puntaje INTEGER, " +
                        "calificadoPor TEXT, " +
                        "calificadoEn TEXT, " +
                        "reasignadoPor TEXT, " +
                        "reasignadoEn TEXT)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS perfiles (" +
                        "id TEXT NOT NULL PRIMARY KEY, " +
                        "nombre TEXT NOT NULL, " +
                        "correo TEXT NOT NULL, " +
                        "telefono TEXT NOT NULL, " +
                        "zona TEXT NOT NULL, " +
                        "idiomas TEXT NOT NULL)"
                )
                db.execSQL("DROP TABLE IF EXISTS servicios")
            }
        }

        val MIGRACION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN ubicacionTermino TEXT")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN latitudInicio REAL")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN longitudInicio REAL")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN precisionInicioMetros REAL")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN distanciaInicioMetros REAL")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN latitudTermino REAL")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN longitudTermino REAL")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN precisionTerminoMetros REAL")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN ultimaLatitud REAL")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN ultimaLongitud REAL")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN ultimaPrecisionMetros REAL")
                db.execSQL("ALTER TABLE estado_servicios ADD COLUMN ultimaUbicacionEn TEXT")
            }
        }

        val MIGRACION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS operaciones_pendientes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "tipo TEXT NOT NULL, " +
                        "servicioId TEXT, " +
                        "detalle TEXT NOT NULL, " +
                        "creadoEn TEXT NOT NULL, " +
                        "estado TEXT NOT NULL, " +
                        "intentos INTEGER NOT NULL)"
                )
            }
        }

        val MIGRACION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE operaciones_pendientes ADD COLUMN idempotencyKey TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        @Volatile private var instancia: LinceDatabase? = null

        fun obtener(context: Context): LinceDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                LinceDatabase::class.java,
                "lince.db"
            ).addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4, MIGRACION_4_5, MIGRACION_5_6)
                .build().also { instancia = it }
        }
    }
}
