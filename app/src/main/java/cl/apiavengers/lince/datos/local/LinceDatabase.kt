package cl.apiavengers.lince.datos.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DisponibilidadEntity::class, AgendaImportadaEntity::class], version = 1, exportSchema = true)
abstract class LinceDatabase : RoomDatabase() {
    abstract fun disponibilidadDao(): DisponibilidadDao

    companion object {
        @Volatile private var instancia: LinceDatabase? = null

        fun obtener(context: Context): LinceDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext, LinceDatabase::class.java, "lince.db"
            ).build().also { instancia = it }
        }
    }
}
