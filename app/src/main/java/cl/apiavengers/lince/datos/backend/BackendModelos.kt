package cl.apiavengers.lince.datos.backend

import com.squareup.moshi.Json

data class LoginRequestBackend(
    val email: String,
    val password: String
)

data class UsuarioBackend(
    val id: String,
    val email: String,
    val nombre: String,
    val rol: String
)

data class LoginResponseBackend(
    val token: String,
    @Json(name = "expiraEn") val expiraEn: String,
    val usuario: UsuarioBackend
)

data class HitoRequestBackend(
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

data class CheckInRequestBackend(
    val servicioId: String,
    val geocercaId: String? = null,
    val lat: Double,
    val lng: Double,
    val precisionM: Double? = null,
    val observaciones: String? = null
)

data class CheckOutRequestBackend(
    val servicioId: String,
    val lat: Double,
    val lng: Double,
    val precisionM: Double? = null,
    val observaciones: String? = null
)

data class DisponibilidadRequestBackend(
    val usuarioId: String? = null,
    val fecha: String,
    val motivo: String? = null
)

data class IncidenciaRequestBackend(
    val servicioId: String,
    val categoria: String,
    val descripcion: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val ocurridoEn: String? = null
)

data class HitoBackend(
    val id: String,
    val servicioId: String,
    val usuarioId: String,
    val tipo: String,
    val ocurridoEn: String
)

data class IncidenciaBackend(
    val id: String,
    val servicioId: String,
    val usuarioId: String,
    val categoria: String,
    val descripcion: String
)

data class DisponibilidadBackend(
    val id: String,
    val usuarioId: String,
    val fecha: String
)
