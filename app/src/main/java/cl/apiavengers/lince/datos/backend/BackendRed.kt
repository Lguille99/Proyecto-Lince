package cl.apiavengers.lince.datos.backend

import cl.apiavengers.lince.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object TokenAlmacen {
    @Volatile var token: String? = null
    fun bearer(): String? = token?.let { "Bearer $it" }
    fun limpiar() { token = null }
}

object BackendRed {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    fun api(baseUrl: String = BuildConfig.BACKEND_URL): BackendApi {
        val interceptor = Interceptor { cadena ->
            val peticion = cadena.request().newBuilder()
            TokenAlmacen.bearer()?.let { peticion.header("Authorization", it) }
            cadena.proceed(peticion.build())
        }
        val clienteHttp = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clienteHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BackendApi::class.java)
    }
}
