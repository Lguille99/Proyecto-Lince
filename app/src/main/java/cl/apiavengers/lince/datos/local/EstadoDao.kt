package cl.apiavengers.lince.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class EstadoDao {
    @Query("SELECT * FROM estado_servicios")
    abstract fun consultarServicios(): List<EstadoServicioEntity>

    @Query("SELECT * FROM perfiles")
    abstract fun consultarPerfiles(): List<PerfilEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertarServicios(servicios: List<EstadoServicioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertarPerfiles(perfiles: List<PerfilEntity>)

    @Query("DELETE FROM estado_servicios")
    abstract fun borrarServicios()

    @Query("DELETE FROM perfiles")
    abstract fun borrarPerfiles()

    @Transaction
    open fun reemplazar(
        servicios: List<EstadoServicioEntity>,
        perfiles: List<PerfilEntity>
    ) {
        borrarServicios()
        insertarServicios(servicios)
        borrarPerfiles()
        insertarPerfiles(perfiles)
    }
}
