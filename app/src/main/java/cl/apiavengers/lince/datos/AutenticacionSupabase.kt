package cl.apiavengers.lince.datos

import cl.apiavengers.lince.BuildConfig
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.modelo.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AutenticacionSupabase {
    suspend fun iniciarSesion(correo: String, clave: String): Usuario = withContext(Dispatchers.IO) {
        val base = BuildConfig.SUPABASE_URL.trimEnd('/')
        require(base.startsWith("https://") && BuildConfig.SUPABASE_KEY.isNotBlank()) {
            "Falta configurar la conexión con Supabase."
        }
        val datos = JSONObject().put("email", correo.trim()).put("password", clave)
        val sesion = JSONObject(solicitar("$base/auth/v1/token?grant_type=password", datos = datos))
        val token = sesion.getString("access_token")
        val authId = sesion.getJSONObject("user").getString("id")
        val perfiles = JSONArray(solicitar(
            "$base/rest/v1/perfiles?auth_user_id=eq.$authId&activo=eq.true&select=codigo,nombre,correo,rol",
            token = token
        ))
        require(perfiles.length() == 1) { "Tu cuenta no tiene un perfil activo asignado. Contacta al coordinador." }
        val perfil = perfiles.getJSONObject(0)
        Usuario(
            id = perfil.getString("codigo"),
            nombre = perfil.getString("nombre"),
            cuenta = correo.trim(),
            correo = perfil.getString("correo"),
            rol = Rol.valueOf(perfil.getString("rol"))
        )
    }

    private fun solicitar(url: String, datos: JSONObject? = null, token: String? = null): String {
        val conexion = URL(url).openConnection() as HttpURLConnection
        try {
            conexion.connectTimeout = 15000
            conexion.readTimeout = 15000
            conexion.instanceFollowRedirects = false
            conexion.requestMethod = if (datos == null) "GET" else "POST"
            conexion.setRequestProperty("apikey", BuildConfig.SUPABASE_KEY)
            token?.let { conexion.setRequestProperty("Authorization", "Bearer $it") }
            if (datos != null) {
                conexion.doOutput = true
                conexion.setRequestProperty("Content-Type", "application/json")
                conexion.outputStream.use { it.write(datos.toString().toByteArray(Charsets.UTF_8)) }
            }
            val estado = conexion.responseCode
            if (estado !in 200..299) {
                val mensaje = when {
                    estado == 429 -> "Demasiados intentos. Espera unos minutos."
                    datos != null && estado == 400 -> "Correo o contraseña incorrectos, o correo sin confirmar."
                    estado == 401 || estado == 403 -> "No se pudo autorizar el acceso. Revisa la configuración de Supabase."
                    else -> "Supabase no está disponible. Intenta nuevamente."
                }
                throw IllegalArgumentException(mensaje)
            }
            return conexion.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conexion.disconnect()
        }
    }
}
