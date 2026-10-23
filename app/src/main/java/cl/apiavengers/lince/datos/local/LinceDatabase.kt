package cl.apiavengers.lince.datos.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DisponibilidadEntity::class, AgendaImportadaEntity::class, ServicioEntity::class], version = 2, exportSchema = true)
abstract class LinceDatabase : RoomDatabase() {
    abstract fun servicioDao(): ServicioDao
    abstract fun disponibilidadDao(): DisponibilidadDao

    companion object {
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS servicios (id TEXT NOT NULL PRIMARY KEY, titulo TEXT NOT NULL, tipo TEXT NOT NULL, fecha TEXT NOT NULL, hora TEXT NOT NULL, recogida TEXT NOT NULL, destino TEXT NOT NULL, asignadoA TEXT NOT NULL, estado TEXT NOT NULL, historico INTEGER NOT NULL, motivoRechazo TEXT, respondidoEn TEXT)")
            }
        }

        @Volatile private var instancia: LinceDatabase? = null

        fun obtener(context: Context): LinceDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext, LinceDatabase::class.java, "lince.db"
            ).addMigrations(MIGRACION_1_2).build().also { instancia = it }
        }
    }
}
