package cl.apiavengers.lince.datos.local

import androidx.room.Entity

@Entity(tableName = "disponibilidad", primaryKeys = ["cuenta", "fecha"])
data class DisponibilidadEntity(val cuenta: String, val fecha: String)

@Entity(tableName = "agendas_importadas", primaryKeys = ["cuenta"])
data class AgendaImportadaEntity(val cuenta: String)
