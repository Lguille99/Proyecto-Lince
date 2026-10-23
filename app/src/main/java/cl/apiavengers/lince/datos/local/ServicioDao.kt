package cl.apiavengers.lince.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ServicioDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertarIniciales(servicios: List<ServicioEntity>)

    @Query("SELECT * FROM servicios ORDER BY fecha, hora, id")
    fun consultar(): List<ServicioEntity>

    @Query("UPDATE servicios SET estado = :estado, motivoRechazo = :motivo, respondidoEn = :fechaHora WHERE id = :id AND asignadoA = :usuarioId AND estado = 'PENDIENTE' AND historico = 0")
    fun responder(id: String, usuarioId: String, estado: String, motivo: String?, fechaHora: String): Int
}
