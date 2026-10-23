package cl.apiavengers.lince.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class DisponibilidadDao {
    @Query("SELECT fecha FROM disponibilidad WHERE cuenta = :cuenta ORDER BY fecha")
    abstract fun consultar(cuenta: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertar(dias: List<DisponibilidadEntity>)

    @Query("DELETE FROM disponibilidad WHERE cuenta = :cuenta")
    abstract fun borrarAgenda(cuenta: String)

    @Query("SELECT EXISTS(SELECT 1 FROM agendas_importadas WHERE cuenta = :cuenta)")
    abstract fun estaImportada(cuenta: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun marcarImportada(agenda: AgendaImportadaEntity)

    @Transaction
    open fun reemplazar(cuenta: String, fechas: List<String>) {
        borrarAgenda(cuenta)
        insertar(fechas.map { DisponibilidadEntity(cuenta, it) })
    }

    @Transaction
    open fun importar(cuenta: String, fechas: List<String>) {
        if (estaImportada(cuenta)) return
        insertar(fechas.map { DisponibilidadEntity(cuenta, it) })
        marcarImportada(AgendaImportadaEntity(cuenta))
    }
}
