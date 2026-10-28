package cl.apiavengers.lince.datos

import cl.apiavengers.lince.BuildConfig
import cl.apiavengers.lince.modelo.Disponibilidad
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.modelo.Modalidad
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ErrorSupabase(
    message: String,
    val esCredencial: Boolean = false,
    val esConexion: Boolean = false
) : Exception(message)

data class PanelLince(
    val servicios: List<Servicio>,
    val equipo: List<Usuario>
)

object SupabaseConfig {
    val url: String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    val clave: String = BuildConfig.SUPABASE_KEY.trim()
    val disponible: Boolean = url.startsWith("https://") && clave.isNotBlank()
}

object SesionSupabase {
    var cliente: ClienteSupabase? = null
    val activa: Boolean get() = cliente?.activo == true
}

class ClienteSupabase {
    private var token: String? = null
    private var perfilUuid: String? = null
    private var usuarioActual: Usuario? = null
    private val perfilesPorCodigo = mutableMapOf<String, String>()
    private val perfilesPorUuid = mutableMapOf<String, Usuario>()
    private val asignacionesPorServicio = mutableMapOf<String, String>()

    val activo: Boolean get() = token != null
    val usuario: Usuario? get() = usuarioActual

    suspend fun iniciarSesion(cuenta: String, clave: String): Usuario {
        exigirConfiguracion()
        val identificador = cuenta.trim()
        require(identificador.isNotEmpty() && clave.isNotEmpty()) {
            "Ingresa tu usuario y contraseña."
        }
        val correo = if (identificador.contains("@")) identificador else "$identificador@lince.local"
        val respuesta = try {
            solicitar(
                url = "${SupabaseConfig.url}/auth/v1/token?grant_type=password",
                metodo = "POST",
                cuerpo = JSONObject().put("email", correo).put("password", clave),
                token = null,
                esLogin = true
            )
        } catch (error: ErrorSupabase) {
            token = null
            throw error
        }
        val sesion = JSONObject(respuesta)
        val accessToken = sesion.getString("access_token")
        val authUserId = sesion.getJSONObject("user").getString("id")
        token = accessToken
        val perfiles = consultar(
            "perfiles?select=*&auth_user_id=eq.${codificar(authUserId)}&activo=eq.true"
        )
        if (perfiles.length() != 1) {
            token = null
            throw ErrorSupabase(
                "Tu cuenta de acceso no tiene un perfil activo en Supabase. " +
                    "Revisa la vinculación de usuarios.",
                esCredencial = true
            )
        }
        val perfil = perfiles.getJSONObject(0)
        perfilUuid = perfil.getString("id")
        val usuario = perfilAUsuario(perfil, identificador)
        usuarioActual = usuario
        perfilesPorUuid.clear()
        perfilesPorCodigo.clear()
        perfilesPorUuid[perfilUuid!!] = usuario
        perfilesPorCodigo[usuario.id] = perfilUuid!!
        return usuario
    }

    suspend fun cargarPanel(): PanelLince {
        exigirSesion()
        val perfilesJson = consultar("perfiles?select=*&activo=eq.true")
        perfilesPorUuid.clear()
        perfilesPorCodigo.clear()
        val equipo = mutableListOf<Usuario>()
        for (indice in 0 until perfilesJson.length()) {
            val perfil = perfilesJson.getJSONObject(indice)
            val uuid = perfil.getString("id")
            val usuario = perfilAUsuario(perfil)
            perfilesPorUuid[uuid] = usuario
            perfilesPorCodigo[usuario.id] = uuid
            equipo += usuario
        }
        val uuidActual = perfilUuid
        usuarioActual?.let { actual ->
            if (uuidActual != null) {
                perfilesPorUuid[uuidActual] = actual
                perfilesPorCodigo[actual.id] = uuidActual
            }
            val reemplazo = equipo.indexOfFirst { it.id == actual.id }
            if (reemplazo >= 0) equipo[reemplazo] = actual else equipo += actual
        }

        val serviciosJson = consultar("servicios?select=*")
        val asignacionesJson = consultar("asignaciones?select=*")
        val ejecucionesJson = consultar("ejecuciones?select=*")
        val calificacionesJson = consultar("calificaciones?select=*")

        val asignaciones = mutableMapOf<String, JSONObject>()
        for (indice in 0 until asignacionesJson.length()) {
            val asignacion = asignacionesJson.getJSONObject(indice)
            val servicioId = asignacion.getString("servicio_id")
            val actual = asignaciones[servicioId]
            val esPropia = asignacion.optString("usuario_id") == perfilUuid
            if (actual == null || esPropia) asignaciones[servicioId] = asignacion
        }
        asignacionesPorServicio.clear()

        val ejecuciones = mutableMapOf<String, JSONObject>()
        for (indice in 0 until ejecucionesJson.length()) {
            val ejecucion = ejecucionesJson.getJSONObject(indice)
            ejecuciones[ejecucion.getString("asignacion_id")] = ejecucion
        }
        val calificaciones = mutableMapOf<String, JSONObject>()
        for (indice in 0 until calificacionesJson.length()) {
            val calificacion = calificacionesJson.getJSONObject(indice)
            calificaciones[calificacion.getString("asignacion_id")] = calificacion
        }

        val servicios = mutableListOf<Servicio>()
        for (indice in 0 until serviciosJson.length()) {
            val servicioJson = serviciosJson.getJSONObject(indice)
            val servicioId = servicioJson.getString("id")
            val asignacion = asignaciones[servicioId]
            if (asignacion != null) {
                asignacionesPorServicio[servicioJson.getString("codigo")] = asignacion.getString("id")
            }
            val ejecucion = asignacion?.let { ejecuciones[it.optString("id")] }
            val calificacion = asignacion?.let { calificaciones[it.optString("id")] }
            servicios += servicioDesde(servicioJson, asignacion, ejecucion, calificacion)
        }

        return PanelLince(
            servicios = servicios.sortedWith(compareBy({ it.fecha }, { it.hora })),
            equipo = equipo.sortedBy { it.id }
        )
    }

    suspend fun cargarDisponibilidad(): Set<LocalDate> {
        exigirSesion()
        val uuid = perfilUuid ?: return emptySet()
        val filas = consultar(
            "disponibilidad?select=fecha,bloqueado&usuario_id=eq.${codificar(uuid)}&bloqueado=eq.true"
        )
        return buildSet {
            for (indice in 0 until filas.length()) {
                val fila = filas.getJSONObject(indice)
                runCatching { LocalDate.parse(fila.getString("fecha")) }.getOrNull()?.let(::add)
            }
        }
    }

    suspend fun guardarDisponibilidad(fecha: LocalDate, bloqueado: Boolean) {
        rpc(
            "guardar_disponibilidad",
            JSONObject().put("p_fecha", fecha.toString()).put("p_bloqueado", bloqueado)
        )
    }

    suspend fun responder(servicioId: String, confirmar: Boolean, motivo: String? = null) {
        val cuerpo = JSONObject()
            .put("p_asignacion", asignacion(servicioId))
            .put("p_confirmar", confirmar)
            .put("p_motivo", if (confirmar) JSONObject.NULL else motivo?.trim().orEmpty())
        rpc("responder_asignacion", cuerpo)
    }

    suspend fun registrarCheckIn(servicioId: String, ubicacion: String, observaciones: String) {
        rpc(
            "registrar_check_in",
            JSONObject()
                .put("p_asignacion", asignacion(servicioId))
                .put("p_ubicacion", ubicacion.trim())
                .put("p_observaciones", observaciones.trim())
        )
    }

    suspend fun registrarCheckOut(servicioId: String, observaciones: String) {
        rpc(
            "registrar_check_out",
            JSONObject()
                .put("p_asignacion", asignacion(servicioId))
                .put("p_observaciones", observaciones.trim())
        )
    }

    suspend fun calificar(servicioId: String, puntaje: Int) {
        rpc(
            "calificar_asignacion",
            JSONObject()
                .put("p_asignacion", asignacion(servicioId))
                .put("p_puntaje", puntaje)
        )
    }

    suspend fun reasignar(servicioId: String, nuevoUsuarioId: String) {
        val nuevoUuid = perfilesPorCodigo[nuevoUsuarioId]
            ?: throw ErrorSupabase("La persona seleccionada ya no está disponible. Actualiza los datos.")
        rpc(
            "reasignar_asignacion",
            JSONObject()
                .put("p_asignacion", asignacion(servicioId))
                .put("p_nuevo_usuario", nuevoUuid)
        )
    }

    suspend fun actualizarPerfil(
        nombre: String,
        correo: String,
        telefono: String,
        zona: String,
        idiomas: List<Idioma>
    ) {
        exigirSesion()
        val arreglo = JSONArray()
        idiomas.forEach { arreglo.put(it.name) }
        rpc(
            "actualizar_mi_perfil",
            JSONObject()
                .put("p_nombre", nombre.trim())
                .put("p_correo", correo.trim())
                .put("p_telefono", telefono.trim())
                .put("p_zona", zona.trim())
                .put("p_idiomas", arreglo)
        )
        usuarioActual = usuarioActual?.copy(
            nombre = nombre.trim(),
            correo = correo.trim(),
            telefono = telefono.trim(),
            zona = zona.trim(),
            idiomas = idiomas
        )
    }

    suspend fun simularSincronizacion() {
        rpc("simular_sincronizacion", JSONObject())
    }

    fun cerrarSesion() {
        token = null
        perfilUuid = null
        usuarioActual = null
        perfilesPorCodigo.clear()
        perfilesPorUuid.clear()
        asignacionesPorServicio.clear()
    }

    private fun asignacion(servicioId: String): String =
        asignacionesPorServicio[servicioId]
            ?: throw ErrorSupabase("No se encontró la asignación remota. Actualiza los datos.")

    private suspend fun rpc(nombre: String, cuerpo: JSONObject) {
        exigirSesion()
        solicitar(
            url = "${SupabaseConfig.url}/rest/v1/rpc/$nombre",
            metodo = "POST",
            cuerpo = cuerpo,
            token = token,
            esLogin = false
        )
    }

    private suspend fun consultar(ruta: String): JSONArray {
        exigirSesion()
        val texto = solicitar(
            url = "${SupabaseConfig.url}/rest/v1/$ruta",
            metodo = "GET",
            cuerpo = null,
            token = token,
            esLogin = false
        )
        return JSONArray(texto.ifBlank { "[]" })
    }

    private suspend fun solicitar(
        url: String,
        metodo: String,
        cuerpo: JSONObject?,
        token: String?,
        esLogin: Boolean
    ): String = withContext(Dispatchers.IO) {
        var conexion: HttpURLConnection? = null
        try {
            conexion = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = metodo
                connectTimeout = 15_000
                readTimeout = 20_000
                setRequestProperty("apikey", SupabaseConfig.clave)
                setRequestProperty("Accept", "application/json")
                if (token != null) setRequestProperty("Authorization", "Bearer $token")
                if (cuerpo != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }
            }
            if (cuerpo != null) {
                conexion.outputStream.use { salida ->
                    salida.write(cuerpo.toString().toByteArray(Charsets.UTF_8))
                }
            }
            val codigo = conexion.responseCode
            val texto = (if (codigo in 200..299) conexion.inputStream else conexion.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (codigo !in 200..299) {
                val mensaje = mensajeError(texto, codigo)
                throw ErrorSupabase(
                    mensaje,
                    esCredencial = esLogin && codigo in 400..401,
                    esConexion = false
                )
            }
            texto
        } catch (error: ErrorSupabase) {
            throw error
        } catch (error: Exception) {
            throw ErrorSupabase(
                "No se pudo conectar con Supabase. Revisa tu conexión e inténtalo nuevamente.",
                esConexion = true
            )
        } finally {
            conexion?.disconnect()
        }
    }

    private fun mensajeError(texto: String, codigo: Int): String {
        val detalle = runCatching {
            val json = JSONObject(texto)
            listOf("message", "msg", "error_description", "hint")
                .firstNotNullOfOrNull { clave ->
                    if (json.has(clave) && !json.isNull(clave)) json.optString(clave) else null
                }
        }.getOrNull()
        if (!detalle.isNullOrBlank()) {
            if (detalle.contains("Invalid login credentials", ignoreCase = true)) {
                return "Usuario o contraseña incorrectos."
            }
            return detalle
        }
        return when (codigo) {
            400, 401 -> "Usuario o contraseña incorrectos."
            403 -> "No tienes permisos para realizar esta operación."
            404 -> "El recurso solicitado no existe en Supabase."
            else -> "Supabase respondió con un error ($codigo). Inténtalo nuevamente."
        }
    }

    private fun perfilAUsuario(perfil: JSONObject, cuenta: String? = null): Usuario {
        val correo = texto(perfil, "correo").orEmpty()
        return Usuario(
            id = perfil.getString("codigo"),
            nombre = perfil.getString("nombre"),
            cuenta = cuenta ?: correo,
            correo = correo,
            rol = enumSeguro(perfil.optString("rol"), Rol.CONDUCTOR),
            telefono = texto(perfil, "telefono").orEmpty(),
            disponibilidad = enumSeguro(
                perfil.optString("estado_disponibilidad"),
                Disponibilidad.DISPONIBLE
            ),
            zona = texto(perfil, "zona").orEmpty(),
            idiomas = idiomas(perfil.optJSONArray("idiomas"))
        )
    }

    private fun servicioDesde(
        servicio: JSONObject,
        asignacion: JSONObject?,
        ejecucion: JSONObject?,
        calificacion: JSONObject?
    ): Servicio {
        val usuarioId = asignacion?.optString("usuario_id").orEmpty()
        val acompananteId = asignacion?.let { texto(it, "acompanante_id") }
        val respondidoEn = asignacion?.let { fechaHora(texto(it, "respondido_en")) }
        val inicioEn = ejecucion?.let { fechaHora(texto(it, "inicio_en")) }
        val terminoEn = ejecucion?.let { fechaHora(texto(it, "termino_en")) }
        val estadoBase = asignacion?.optString("estado").orEmpty()
        val estado = when {
            estadoBase == "RECHAZADO" -> EstadoServicio.RECHAZADA
            ejecucion != null && !texto(ejecucion, "termino_en").isNullOrBlank() -> EstadoServicio.COMPLETADA
            estadoBase == "CONFIRMADO" -> EstadoServicio.CONFIRMADA
            else -> EstadoServicio.PENDIENTE
        }
        val observacionesInicio = ejecucion?.let { texto(it, "observaciones_inicio").orEmpty() }.orEmpty()
        val observacionesTermino = ejecucion?.let { texto(it, "observaciones_termino").orEmpty() }.orEmpty()
        val hora = servicio.optString("hora").take(5)
        return Servicio(
            id = servicio.getString("codigo"),
            titulo = servicio.getString("titulo"),
            tipo = servicio.getString("tipo"),
            fecha = servicio.getString("fecha"),
            hora = hora,
            recogida = servicio.getString("recogida"),
            destino = servicio.getString("destino"),
            asignadoA = perfilesPorUuid[usuarioId]?.id.orEmpty(),
            estado = estado,
            acompananteId = acompananteId?.let { perfilesPorUuid[it]?.id },
            idiomaRequerido = enumSeguro(servicio.optString("idioma_requerido"), Idioma.ESPANOL),
            modalidad = enumSeguro(servicio.optString("modalidad"), Modalidad.TRANSFER),
            almuerzoIncluido = servicio.optBoolean("almuerzo_incluido", false),
            itinerario = textos(servicio.optJSONArray("itinerario")),
            resenaHistorica = texto(servicio, "resena_historica").orEmpty(),
            hotelRecogida = texto(servicio, "hotel_recogida") ?: servicio.getString("recogida"),
            tarifa = servicio.optInt("tarifa", 0),
            pasajeros = servicio.optInt("pasajeros", 1),
            diasTour = servicio.optInt("dias_tour", 1),
            respondidoPor = if (respondidoEn != null) perfilesPorUuid[usuarioId]?.id else null,
            respondidoEn = respondidoEn,
            motivoRechazo = asignacion?.let { texto(it, "motivo_rechazo") },
            inicioEn = inicioEn,
            terminoEn = terminoEn,
            inicioPor = if (inicioEn != null) perfilesPorUuid[usuarioId]?.id else null,
            ubicacionInicio = ejecucion?.let { texto(it, "ubicacion_inicio") },
            observaciones = observacionesTermino.ifBlank { observacionesInicio },
            puntaje = calificacion?.optInt("puntaje", 0)?.takeIf { it in 1..5 },
            calificadoPor = calificacion?.let { codigoDe(texto(it, "calificado_por")) },
            calificadoEn = calificacion?.let { fechaHora(texto(it, "calificado_en")) },
            reasignadoPor = asignacion?.let { codigoDe(texto(it, "reasignado_por")) },
            reasignadoEn = asignacion?.let { fechaHora(texto(it, "reasignado_en")) }
        )
    }

    private fun fechaHora(valor: String?): String? {
        if (valor.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(valor)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
                .toString()
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(valor).toString()
            } catch (_: DateTimeParseException) {
                valor
            }
        }
    }

    private fun idiomas(arreglo: JSONArray?): List<Idioma> {
        if (arreglo == null || arreglo.length() == 0) return listOf(Idioma.ESPANOL)
        return buildList {
            for (indice in 0 until arreglo.length()) {
                add(enumSeguro(arreglo.optString(indice), Idioma.ESPANOL))
            }
        }.distinct().ifEmpty { listOf(Idioma.ESPANOL) }
    }

    private fun textos(arreglo: JSONArray?): List<String> {
        if (arreglo == null) return emptyList()
        return buildList {
            for (indice in 0 until arreglo.length()) {
                arreglo.optString(indice).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }

    private fun texto(objeto: JSONObject, clave: String): String? {
        if (!objeto.has(clave) || objeto.isNull(clave)) return null
        return objeto.optString(clave)
    }

    private fun codigoDe(uuid: String?): String? = uuid?.let { perfilesPorUuid[it]?.id }

    private inline fun <reified T : Enum<T>> enumSeguro(valor: String?, defecto: T): T =
        runCatching { enumValueOf<T>(valor.orEmpty()) }.getOrDefault(defecto)

    private fun codificar(valor: String): String = java.net.URLEncoder.encode(valor, "UTF-8")

    private fun exigirConfiguracion() {
        if (!SupabaseConfig.disponible) {
            throw ErrorSupabase(
                "Supabase no está configurado. Agrega SUPABASE_URL y SUPABASE_KEY en local.properties.",
                esConexion = true
            )
        }
    }

    private fun exigirSesion() {
        if (token == null) {
            throw ErrorSupabase("Debes iniciar sesión nuevamente.")
        }
    }
}
