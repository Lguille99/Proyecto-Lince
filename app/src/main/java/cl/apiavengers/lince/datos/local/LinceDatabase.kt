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
        PerfilEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class LinceDatabase : RoomDatabase() {
    abstract fun estadoDao(): EstadoDao
    abstract fun disponibilidadDao(): DisponibilidadDao

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

        @Volatile private var instancia: LinceDatabase? = null

        fun obtener(context: Context): LinceDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                LinceDatabase::class.java,
                "lince.db"
            ).addMigrations(MIGRACION_1_2, MIGRACION_2_3).build().also { instancia = it }
        }
    }
}
