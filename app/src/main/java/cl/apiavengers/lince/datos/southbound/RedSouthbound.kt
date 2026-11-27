package cl.apiavengers.lince.datos.southbound

import cl.apiavengers.lince.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Contrato Retrofit de solo lectura para la API de Southbound.
 * La ruta se resuelve contra el host base y conserva los query params ya codificados.
 */
interface SouthboundApi {
    @GET
    fun obtener(@Url ruta: String): Call<ResponseBody>
}

/**
 * Construye el cliente HTTP con OkHttp + Retrofit. La clave se inyecta en un
 * [Interceptor] leyendo [BuildConfig.SOUTHBOUND_API_KEY]; nunca se escribe en el código.
 */
object RedSouthbound {
    private const val BASE_URL = "https://ws.southbound.travel/lince-api/"

    fun api(clave: String = BuildConfig.SOUTHBOUND_API_KEY): SouthboundApi {
        val interceptor = Interceptor { cadena ->
            val peticion = cadena.request().newBuilder()
                .header("Accept", "application/json")
                .apply { if (clave.isNotBlank()) header("X-API-Key", clave) }
                .build()
            cadena.proceed(peticion)
        }
        val clienteHttp = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clienteHttp)
            .build()
            .create(SouthboundApi::class.java)
    }
}

/** Implementación de transporte basada en Retrofit que reemplaza a HttpURLConnection. */
class TransporteRetrofit(
    private val api: SouthboundApi = RedSouthbound.api()
) : TransporteSouthbound {
    override fun consultar(ruta: String, clave: String): RespuestaHttp {
        val respuesta = api.obtener(ruta.trimStart('/')).execute()
        val cuerpo = if (respuesta.isSuccessful) {
            respuesta.body()?.string().orEmpty()
        } else {
            respuesta.errorBody()?.string().orEmpty()
        }
        return RespuestaHttp(respuesta.code(), cuerpo)
    }
}
