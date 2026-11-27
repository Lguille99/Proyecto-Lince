package cl.apiavengers.lince.backend.modelo

import kotlinx.serialization.Serializable

@Serializable
data class ErrorRespuesta(
    val codigo: String,
    val mensaje: String,
    val requestId: String? = null
)

@Serializable
data class SaludRespuesta(
    val estado: String,
    val version: String
)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class CrearUsuarioRequest(
    val email: String,
    val nombre: String,
    val rol: String,
    val password: String
)

@Serializable
data class ActualizarUsuarioRequest(
    val email: String? = null,
    val nombre: String? = null,
    val rol: String? = null,
    val password: String? = null
)

@Serializable
data class UsuarioDto(
    val id: String,
    val email: String,
    val nombre: String,
    val rol: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val expiraEn: String,
    val usuario: UsuarioDto
)

@Serializable
data class GeocercaRequest(
    val servicioId: String? = null,
    val tipo: String,
    val nombre: String,
    val lat: Double,
    val lng: Double,
    val radioM: Int = 150
)

@Serializable
data class GeocercaDto(
    val id: String,
    val servicioId: String?,
    val tipo: String,
    val nombre: String,
    val lat: Double,
    val lng: Double,
    val radioM: Int,
    val creadoPor: String,
    val creadoEn: String,
    val actualizadoEn: String
)

@Serializable
data class HitoRequest(
    val servicioId: String,
    val tipo: String,
    val geocercaId: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val precisionM: Double? = null,
    val ocurridoEn: String? = null,
    val observaciones: String? = null,
    val motivoRechazo: String? = null
)

@Serializable
data class CheckInRequest(
    val servicioId: String,
    val geocercaId: String? = null,
    val lat: Double,
    val lng: Double,
    val precisionM: Double? = null,
    val observaciones: String? = null
)

@Serializable
data class CheckOutRequest(
    val servicioId: String,
    val lat: Double,
    val lng: Double,
    val precisionM: Double? = null,
    val observaciones: String? = null
)

@Serializable
data class HitoDto(
    val id: String,
    val servicioId: String,
    val geocercaId: String?,
    val usuarioId: String,
    val tipo: String,
    val lat: Double?,
    val lng: Double?,
    val precisionM: Double?,
    val distanciaM: Double?,
    val ocurridoEn: String,
    val observaciones: String?,
    val motivoRechazo: String?,
    val requestId: String?,
    val estadoSync: String
)

@Serializable
data class ActualizarHitoRequest(
    val observaciones: String? = null,
    val motivoRechazo: String? = null
)

@Serializable
data class DisponibilidadRequest(
    val usuarioId: String? = null,
    val fecha: String,
    val motivo: String? = null
)

@Serializable
data class DisponibilidadDto(
    val id: String,
    val usuarioId: String,
    val fecha: String,
    val motivo: String?,
    val creadoEn: String,
    val estado: String
)

@Serializable
data class IncidenciaRequest(
    val servicioId: String,
    val categoria: String,
    val descripcion: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val ocurridoEn: String? = null
)

@Serializable
data class IncidenciaDto(
    val id: String,
    val servicioId: String,
    val usuarioId: String,
    val categoria: String,
    val descripcion: String,
    val lat: Double?,
    val lng: Double?,
    val ocurridoEn: String,
    val creadoEn: String,
    val estado: String
)

@Serializable
data class ActualizarIncidenciaRequest(val estado: String)

@Serializable
data class UltimaPosicionDto(
    val servicioId: String,
    val lat: Double,
    val lng: Double,
    val precisionM: Double?,
    val reportadoEn: String,
    val tipoEvento: String,
    val usuarioId: String
)

@Serializable
data class PaginaMeta(
    val pagina: Int,
    val limite: Int,
    val total: Int,
    val paginas: Int
)

@Serializable
data class PaginaDto<T>(
    val data: List<T>,
    val meta: PaginaMeta
)
