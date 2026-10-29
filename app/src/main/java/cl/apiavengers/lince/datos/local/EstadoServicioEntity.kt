package cl.apiavengers.lince.datos.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.apiavengers.lince.datos.CambioPerfil
import cl.apiavengers.lince.datos.CambioServicio
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma

@Entity(tableName = "estado_servicios")
data class EstadoServicioEntity(
    @PrimaryKey val id: String,
    val estado: String,
    val asignadoA: String,
    val acompananteId: String?,
    val respondidoPor: String?,
    val respondidoEn: String?,
    val motivoRechazo: String?,
    val inicioEn: String?,
    val terminoEn: String?,
    val inicioPor: String?,
    val ubicacionInicio: String?,
    val observaciones: String,
    val puntaje: Int?,
    val calificadoPor: String?,
    val calificadoEn: String?,
    val reasignadoPor: String?,
    val reasignadoEn: String?
) {
    fun aModelo(): CambioServicio = CambioServicio(
        estado = enumSeguro(estado, EstadoServicio.PENDIENTE),
        asignadoA = asignadoA,
        acompananteId = acompananteId,
        respondidoPor = respondidoPor,
        respondidoEn = respondidoEn,
        motivoRechazo = motivoRechazo,
        inicioEn = inicioEn,
        terminoEn = terminoEn,
        inicioPor = inicioPor,
        ubicacionInicio = ubicacionInicio,
        observaciones = observaciones,
        puntaje = puntaje,
        calificadoPor = calificadoPor,
        calificadoEn = calificadoEn,
        reasignadoPor = reasignadoPor,
        reasignadoEn = reasignadoEn
    )

    companion object {
        fun desde(id: String, cambio: CambioServicio) = EstadoServicioEntity(
            id = id,
            estado = cambio.estado.name,
            asignadoA = cambio.asignadoA,
            acompananteId = cambio.acompananteId,
            respondidoPor = cambio.respondidoPor,
            respondidoEn = cambio.respondidoEn,
            motivoRechazo = cambio.motivoRechazo,
            inicioEn = cambio.inicioEn,
            terminoEn = cambio.terminoEn,
            inicioPor = cambio.inicioPor,
            ubicacionInicio = cambio.ubicacionInicio,
            observaciones = cambio.observaciones,
            puntaje = cambio.puntaje,
            calificadoPor = cambio.calificadoPor,
            calificadoEn = cambio.calificadoEn,
            reasignadoPor = cambio.reasignadoPor,
            reasignadoEn = cambio.reasignadoEn
        )
    }
}

@Entity(tableName = "perfiles")
data class PerfilEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val zona: String,
    val idiomas: String
) {
    fun aModelo(): CambioPerfil {
        val idiomasModelo = idiomas.split(",")
            .filter { it.isNotBlank() }
            .map { enumSeguro(it, Idioma.ESPANOL) }
            .ifEmpty { listOf(Idioma.ESPANOL) }
        return CambioPerfil(
            nombre = nombre,
            correo = correo,
            telefono = telefono,
            zona = zona,
            idiomas = idiomasModelo
        )
    }

    companion object {
        fun desde(id: String, cambio: CambioPerfil) = PerfilEntity(
            id = id,
            nombre = cambio.nombre,
            correo = cambio.correo,
            telefono = cambio.telefono,
            zona = cambio.zona,
            idiomas = cambio.idiomas.joinToString(",") { it.name }
        )
    }
}

private inline fun <reified T : Enum<T>> enumSeguro(valor: String?, porDefecto: T): T =
    runCatching { enumValueOf<T>(valor.orEmpty()) }.getOrDefault(porDefecto)
