package cl.apiavengers.lince.datos.southbound

import android.content.Context
import androidx.room.*

@Entity(tableName = "consultas")
data class ConsultaSouthbound(
    @PrimaryKey val ruta: String,
    val contenido: String,
    val guardadoEn: Long
)

@Dao
interface CacheSouthboundDao {
    @Query("SELECT * FROM consultas WHERE ruta = :ruta")
    fun leer(ruta: String): ConsultaSouthbound?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardar(consulta: ConsultaSouthbound)
}

@Database(entities = [ConsultaSouthbound::class], version = 1, exportSchema = true)
abstract class CacheSouthbound : RoomDatabase() {
    abstract fun consultas(): CacheSouthboundDao

    companion object {
        @Volatile private var instancia: CacheSouthbound? = null

        fun obtener(contexto: Context): CacheSouthbound = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                contexto.applicationContext, CacheSouthbound::class.java, "southbound.db"
            ).build().also { instancia = it }
        }
    }
}
