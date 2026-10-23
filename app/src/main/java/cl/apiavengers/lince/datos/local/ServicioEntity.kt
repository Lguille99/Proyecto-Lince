package cl.apiavengers.lince.datos.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.EstadoServicio

@Entity(tableName = "servicios")
data class ServicioEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val tipo: String,
    val fecha: String,
    val hora: String,
    val recogida: String,
    val destino: String,
    val asignadoA: String,
    val estado: String,
    val historico: Boolean,
    val motivoRechazo: String?,
    val respondidoEn: String?
) {
    fun aModelo() = Servicio(id, titulo, tipo, fecha, hora, recogida, destino, asignadoA,
        EstadoServicio.valueOf(estado), historico, motivoRechazo, respondidoEn)

    companion object {
        fun desde(s: Servicio) = ServicioEntity(s.id, s.titulo, s.tipo, s.fecha, s.hora, s.recogida,
            s.destino, s.asignadoA, s.estado.name, s.historico, s.motivoRechazo, s.respondidoEn)
    }
}
