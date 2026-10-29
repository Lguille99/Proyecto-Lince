package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma

data class CambioServicio(
    val estado: EstadoServicio,
    val asignadoA: String,
    val acompananteId: String? = null,
    val respondidoPor: String? = null,
    val respondidoEn: String? = null,
    val motivoRechazo: String? = null,
    val inicioEn: String? = null,
    val terminoEn: String? = null,
    val inicioPor: String? = null,
    val ubicacionInicio: String? = null,
    val observaciones: String = "",
    val puntaje: Int? = null,
    val calificadoPor: String? = null,
    val calificadoEn: String? = null,
    val reasignadoPor: String? = null,
    val reasignadoEn: String? = null
)

data class CambioPerfil(
    val nombre: String,
    val correo: String,
    val telefono: String,
    val zona: String,
    val idiomas: List<Idioma>
)

data class EstadoGuardado(
    val servicios: Map<String, CambioServicio> = emptyMap(),
    val perfiles: Map<String, CambioPerfil> = emptyMap()
)

interface AlmacenEstado {
    fun leer(): EstadoGuardado
    fun guardar(estado: EstadoGuardado)
}

class AlmacenEstadoMemoria(
    private var estado: EstadoGuardado = EstadoGuardado()
) : AlmacenEstado {
    override fun leer(): EstadoGuardado = estado
    override fun guardar(estado: EstadoGuardado) {
        this.estado = estado
    }
}
