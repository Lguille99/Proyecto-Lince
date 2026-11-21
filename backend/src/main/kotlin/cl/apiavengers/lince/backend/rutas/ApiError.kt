package cl.apiavengers.lince.backend.rutas

import io.ktor.http.HttpStatusCode

class ApiError(
    val status: HttpStatusCode,
    val codigo: String,
    mensaje: String
) : RuntimeException(mensaje)
