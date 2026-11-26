package cl.apiavengers.lince.datos.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

enum class EstadoOperacion {
    PENDIENTE,
    SINCRONIZADA,
    ERROR
}

@Entity(tableName = "operaciones_pendientes")
data class OperacionPendienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,
    val servicioId: String?,
    val detalle: String,
    val creadoEn: String,
    val estado: String = EstadoOperacion.PENDIENTE.name,
    val intentos: Int = 0
)

@Dao
interface OperacionPendienteDao {
    @Insert
    fun insertar(operacion: OperacionPendienteEntity): Long

    @Query("SELECT * FROM operaciones_pendientes WHERE estado = :estado ORDER BY id")
    fun consultarPorEstado(estado: String): List<OperacionPendienteEntity>

    @Query("SELECT COUNT(*) FROM operaciones_pendientes WHERE estado = :estado")
    fun contarPorEstado(estado: String): Int

    @Query("UPDATE operaciones_pendientes SET estado = :estado WHERE id = :id")
    fun actualizarEstado(id: Long, estado: String)

    @Query("UPDATE operaciones_pendientes SET intentos = intentos + 1 WHERE id = :id")
    fun registrarIntento(id: Long)

    @Query("DELETE FROM operaciones_pendientes WHERE estado = :estado")
    fun borrarPorEstado(estado: String)
}
