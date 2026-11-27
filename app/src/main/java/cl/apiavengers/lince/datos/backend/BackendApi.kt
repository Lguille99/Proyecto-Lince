package cl.apiavengers.lince.datos.backend

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface BackendApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestBackend): Response<LoginResponseBackend>

    @POST("api/v1/hitos")
    suspend fun crearHito(
        @Header("Authorization") bearer: String,
        @Body request: HitoRequestBackend
    ): Response<HitoBackend>

    @POST("api/v1/hitos/check-in")
    suspend fun checkIn(
        @Header("Authorization") bearer: String,
        @Body request: CheckInRequestBackend
    ): Response<HitoBackend>

    @POST("api/v1/hitos/check-out")
    suspend fun checkOut(
        @Header("Authorization") bearer: String,
        @Body request: CheckOutRequestBackend
    ): Response<HitoBackend>

    @POST("api/v1/disponibilidad")
    suspend fun bloquearDisponibilidad(
        @Header("Authorization") bearer: String,
        @Body request: DisponibilidadRequestBackend
    ): Response<DisponibilidadBackend>

    @POST("api/v1/incidencias")
    suspend fun crearIncidencia(
        @Header("Authorization") bearer: String,
        @Body request: IncidenciaRequestBackend
    ): Response<IncidenciaBackend>

    @GET("api/v1/coordinacion/servicios/{servicioId}/ultima-posicion")
    suspend fun ultimaPosicion(
        @Header("Authorization") bearer: String,
        @Path("servicioId") servicioId: String
    ): Response<Any>
}
