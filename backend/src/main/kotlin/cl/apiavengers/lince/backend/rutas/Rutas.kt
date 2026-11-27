package cl.apiavengers.lince.backend.rutas

import cl.apiavengers.lince.backend.dominio.Reglas
import cl.apiavengers.lince.backend.modelo.ActualizarHitoRequest
import cl.apiavengers.lince.backend.modelo.ActualizarIncidenciaRequest
import cl.apiavengers.lince.backend.modelo.ActualizarUsuarioRequest
import cl.apiavengers.lince.backend.modelo.CrearUsuarioRequest
import cl.apiavengers.lince.backend.modelo.UsuarioDto
import cl.apiavengers.lince.backend.modelo.CheckInRequest
import cl.apiavengers.lince.backend.modelo.CheckOutRequest
import cl.apiavengers.lince.backend.modelo.DisponibilidadRequest
import cl.apiavengers.lince.backend.modelo.GeocercaRequest
import cl.apiavengers.lince.backend.modelo.HitoRequest
import cl.apiavengers.lince.backend.modelo.IncidenciaRequest
import cl.apiavengers.lince.backend.modelo.LoginRequest
import cl.apiavengers.lince.backend.modelo.LoginResponse
import cl.apiavengers.lince.backend.repositorios.DisponibilidadRepo
import cl.apiavengers.lince.backend.repositorios.aDtoUsuario
import cl.apiavengers.lince.backend.repositorios.GeocercasRepo
import cl.apiavengers.lince.backend.repositorios.HitosRepo
import cl.apiavengers.lince.backend.repositorios.IncidenciasRepo
import cl.apiavengers.lince.backend.repositorios.ServiciosRepo
import cl.apiavengers.lince.backend.repositorios.UsuarioFila
import cl.apiavengers.lince.backend.repositorios.UsuariosRepo
import cl.apiavengers.lince.backend.repositorios.nuevoId
import cl.apiavengers.lince.backend.seguridad.Jwt
import cl.apiavengers.lince.backend.seguridad.Password
import cl.apiavengers.lince.backend.seguridad.RolApi
import cl.apiavengers.lince.backend.seguridad.UsuarioPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDate
import kotlin.math.roundToInt

private enum class TipoHito { CHECK_IN, CHECK_OUT, CONFIRMACION, RECHAZO, REPORTE_POSICION }

fun Route.rutasPublicas() {
    route("/api/v1") {
        post("/auth/login") {
            val request = call.receive<LoginRequest>()
            if (request.email.isBlank() || request.password.isBlank()) {
                throw ApiError(HttpStatusCode.UnprocessableEntity, "SOLICITUD_INVALIDA", "Email y contraseña son obligatorios.")
            }
            val usuario = UsuariosRepo.porEmail(request.email.trim())
                ?: throw ApiError(HttpStatusCode.Unauthorized, "CREDENCIALES_INVALIDAS", "Email o contraseña incorrectos.")
            if (!Password.verificar(request.password, usuario.passwordHash)) {
                throw ApiError(HttpStatusCode.Unauthorized, "CREDENCIALES_INVALIDAS", "Email o contraseña incorrectos.")
            }
            call.respond(
                LoginResponse(
                    token = Jwt.emitir(usuario.principal()),
                    expiraEn = Jwt.expiracion(),
                    usuario = usuario.aDto()
                )
            )
        }
    }
}

fun Route.rutasProtegidas() {
    route("/api/v1") {
        rutaHitos()
        rutaGeocercas()
        rutaDisponibilidad()
        rutaIncidencias()
        rutaCoordinacion()
        rutaUsuarios()
    }
}

private fun Route.rutaHitos() {
    get("/hitos") {
        val principal = call.exigirRol()
        val servicioId = call.request.queryParameters["servicioId"]
        val tipo = call.request.queryParameters["tipo"]
        val usuarioId = if (principal.rol == RolApi.COORDINACION) {
            call.request.queryParameters["usuarioId"]
        } else {
            principal.id
        }
        call.respond(HitosRepo.listar(servicioId, tipo, usuarioId))
    }

    get("/hitos/{id}") {
        val principal = call.exigirRol()
        val hito = HitosRepo.buscar(call.idParametro()) 
            ?: throw ApiError(HttpStatusCode.NotFound, "HITO_NO_ENCONTRADO", "Hito no encontrado.")
        if (principal.rol != RolApi.COORDINACION && hito.usuarioId != principal.id) {
            throw ApiError(HttpStatusCode.Forbidden, "SIN_PERMISO", "No puedes ver hitos de otro usuario.")
        }
        call.respond(hito)
    }

    post("/hitos") {
        val principal = call.exigirRol()
        val request = call.receive<HitoRequest>()
        val tipo = parsearTipo(request.tipo)
        if (tipo == TipoHito.CHECK_IN || tipo == TipoHito.CHECK_OUT) {
            throw ApiError(
                HttpStatusCode.BadRequest, "USA_ENDPOINT_DEDICADO",
                "Usa /hitos/check-in o /hitos/check-out para esos hitos."
            )
        }
        ServiciosRepo.buscar(request.servicioId)
            ?: throw ApiError(HttpStatusCode.NotFound, "SERVICIO_NO_ENCONTRADO", "Servicio no encontrado.")
        val motivo = request.motivoRechazo?.trim()
        if (tipo == TipoHito.RECHAZO && (motivo == null || motivo.length !in 1..300)) {
            throw ApiError(HttpStatusCode.UnprocessableEntity, "MOTIVO_INVALIDO", "El rechazo requiere un motivo de 1 a 300 caracteres.")
        }
        if (tipo == TipoHito.REPORTE_POSICION) validarCoordenadas(request.lat, request.lng, true)
        val ocurridoEn = request.ocurridoEn?.let { Reglas.parsearInstante(it).toString() } ?: Instant.now().toString()
        val hito = HitosRepo.crear(
            id = nuevoId(),
            servicioId = request.servicioId,
            geocercaId = request.geocercaId,
            usuarioId = principal.id,
            tipo = tipo.name,
            lat = request.lat,
            lng = request.lng,
            precisionM = request.precisionM,
            distanciaM = null,
            ocurridoEn = ocurridoEn,
            observaciones = request.observaciones?.trim()?.takeIf { it.isNotEmpty() },
            motivoRechazo = motivo,
            requestId = call.callId
        )
        call.respond(HttpStatusCode.Created, hito)
    }

    put("/hitos/{id}") {
        call.exigirRol(RolApi.COORDINACION)
        val id = call.idParametro()
        HitosRepo.buscar(id) ?: throw ApiError(HttpStatusCode.NotFound, "HITO_NO_ENCONTRADO", "Hito no encontrado.")
        val request = call.receive<ActualizarHitoRequest>()
        call.respond(HitosRepo.actualizar(id, request.observaciones, request.motivoRechazo)!!)
    }

    delete("/hitos/{id}") {
        call.exigirRol(RolApi.COORDINACION)
        val id = call.idParametro()
        if (!HitosRepo.borrar(id)) {
            throw ApiError(HttpStatusCode.NotFound, "HITO_NO_ENCONTRADO", "Hito no encontrado.")
        }
        call.respond(HttpStatusCode.NoContent)
    }

    post("/hitos/check-in") {
        val principal = call.exigirRol(RolApi.GUIA, RolApi.TRANSPORTISTA)
        val request = call.receive<CheckInRequest>()
        validarCoordenadas(request.lat, request.lng, true)
        val servicio = ServiciosRepo.buscar(request.servicioId)
            ?: throw ApiError(HttpStatusCode.NotFound, "SERVICIO_NO_ENCONTRADO", "Servicio no encontrado.")
        if (!ServiciosRepo.estaAsignado(request.servicioId, principal.id)) {
            throw ApiError(HttpStatusCode.Forbidden, "SIN_ASIGNACION", "No estás asignado a este servicio.")
        }
        if (HitosRepo.existeTipo(request.servicioId, TipoHito.CHECK_IN.name)) {
            throw ApiError(HttpStatusCode.Conflict, "CHECK_IN_DUPLICADO", "El servicio ya tiene un check-in.")
        }
        val geocerca = GeocercasRepo.paraServicio(request.servicioId, request.geocercaId)
            ?: throw ApiError(HttpStatusCode.UnprocessableEntity, "GEOCERCA_NO_DEFINIDA", "El servicio no tiene geocerca configurada.")
        val distancia = Reglas.distanciaMetros(request.lat, request.lng, geocerca.lat, geocerca.lng)
        if (distancia > geocerca.radioM) {
            throw ApiError(
                HttpStatusCode.UnprocessableEntity, "FUERA_DE_GEOCERCA",
                "Estás a ${distancia.roundToInt()} m del punto; el radio permitido es ${geocerca.radioM} m."
            )
        }
        val ahora = Instant.now()
        val pickup = Reglas.parsearInstante(servicio.fechaHoraPickup)
        if (!Reglas.esPuntual(pickup, ahora)) {
            throw ApiError(
                HttpStatusCode.UnprocessableEntity, "CHECK_IN_TARDIO",
                "El check-in debe registrarse al menos ${Reglas.MINUTOS_PUNTUALIDAD} minutos antes del pickup " +
                    "(${Reglas.minutosDeTardanza(pickup, ahora)} min de tardanza)."
            )
        }
        val hito = HitosRepo.crear(
            id = nuevoId(),
            servicioId = request.servicioId,
            geocercaId = geocerca.id,
            usuarioId = principal.id,
            tipo = TipoHito.CHECK_IN.name,
            lat = request.lat,
            lng = request.lng,
            precisionM = request.precisionM,
            distanciaM = distancia,
            ocurridoEn = ahora.toString(),
            observaciones = request.observaciones?.trim()?.takeIf { it.isNotEmpty() },
            motivoRechazo = null,
            requestId = call.callId
        )
        call.respond(HttpStatusCode.Created, hito)
    }

    post("/hitos/check-out") {
        val principal = call.exigirRol(RolApi.GUIA, RolApi.TRANSPORTISTA)
        val request = call.receive<CheckOutRequest>()
        validarCoordenadas(request.lat, request.lng, true)
        val servicio = ServiciosRepo.buscar(request.servicioId)
            ?: throw ApiError(HttpStatusCode.NotFound, "SERVICIO_NO_ENCONTRADO", "Servicio no encontrado.")
        if (!ServiciosRepo.estaAsignado(request.servicioId, principal.id)) {
            throw ApiError(HttpStatusCode.Forbidden, "SIN_ASIGNACION", "No estás asignado a este servicio.")
        }
        if (!HitosRepo.existeTipo(request.servicioId, TipoHito.CHECK_IN.name)) {
            throw ApiError(HttpStatusCode.Conflict, "SIN_CHECK_IN", "Primero debes registrar el check-in.")
        }
        if (HitosRepo.existeTipo(request.servicioId, TipoHito.CHECK_OUT.name)) {
            throw ApiError(HttpStatusCode.Conflict, "CHECK_OUT_DUPLICADO", "El servicio ya tiene un check-out.")
        }
        val geocercas = GeocercasRepo.listar(request.servicioId)
        if (geocercas.isNotEmpty()) {
            val dentro = geocercas.any { g ->
                Reglas.distanciaMetros(request.lat, request.lng, g.lat, g.lng) <= g.radioM
            }
            if (!dentro) {
                throw ApiError(
                    HttpStatusCode.UnprocessableEntity, "FUERA_DE_GEOCERCA_DESTINO",
                    "El check-out está fuera de la geocerca de destino configurada."
                )
            }
        }
        val hito = HitosRepo.crear(
            id = nuevoId(),
            servicioId = request.servicioId,
            geocercaId = null,
            usuarioId = principal.id,
            tipo = TipoHito.CHECK_OUT.name,
            lat = request.lat,
            lng = request.lng,
            precisionM = request.precisionM,
            distanciaM = null,
            ocurridoEn = Instant.now().toString(),
            observaciones = request.observaciones?.trim()?.takeIf { it.isNotEmpty() },
            motivoRechazo = null,
            requestId = call.callId
        )
        call.respond(HttpStatusCode.Created, hito)
    }
}

private fun Route.rutaGeocercas() {
    get("/geocercas") {
        call.exigirRol()
        call.respond(GeocercasRepo.listar(call.request.queryParameters["servicioId"]))
    }

    get("/geocercas/{id}") {
        call.exigirRol()
        call.respond(
            GeocercasRepo.buscar(call.idParametro())
                ?: throw ApiError(HttpStatusCode.NotFound, "GEOCERCA_NO_ENCONTRADA", "Geocerca no encontrada.")
        )
    }

    post("/geocercas") {
        val principal = call.exigirRol(RolApi.COORDINACION)
        val request = call.receive<GeocercaRequest>()
        validarGeocerca(request)
        val creada = try {
            GeocercasRepo.crear(nuevoId(), request, principal.id)
        } catch (_: SQLException) {
            throw ApiError(HttpStatusCode.Conflict, "GEOCERCA_DUPLICADA", "Ya existe una geocerca de ese tipo para el servicio.")
        }
        call.respond(HttpStatusCode.Created, creada)
    }

    put("/geocercas/{id}") {
        call.exigirRol(RolApi.COORDINACION)
        val id = call.idParametro()
        val request = call.receive<GeocercaRequest>()
        validarGeocerca(request)
        GeocercasRepo.buscar(id)
            ?: throw ApiError(HttpStatusCode.NotFound, "GEOCERCA_NO_ENCONTRADA", "Geocerca no encontrada.")
        val actualizada = try {
            GeocercasRepo.actualizar(id, request)
        } catch (_: SQLException) {
            throw ApiError(HttpStatusCode.Conflict, "GEOCERCA_DUPLICADA", "Ya existe una geocerca de ese tipo para el servicio.")
        }
        call.respond(actualizada!!)
    }

    delete("/geocercas/{id}") {
        call.exigirRol(RolApi.COORDINACION)
        if (!GeocercasRepo.borrar(call.idParametro())) {
            throw ApiError(HttpStatusCode.NotFound, "GEOCERCA_NO_ENCONTRADA", "Geocerca no encontrada.")
        }
        call.respond(HttpStatusCode.NoContent)
    }
}

private fun Route.rutaDisponibilidad() {
    get("/disponibilidad") {
        val principal = call.exigirRol()
        val usuarioId = if (principal.rol == RolApi.COORDINACION) {
            call.request.queryParameters["usuarioId"]
        } else {
            principal.id
        }
        call.respond(DisponibilidadRepo.listar(usuarioId))
    }

    post("/disponibilidad") {
        val principal = call.exigirRol()
        val request = call.receive<DisponibilidadRequest>()
        val usuarioId = when {
            principal.rol == RolApi.COORDINACION && !request.usuarioId.isNullOrBlank() -> request.usuarioId
            else -> principal.id
        }
        UsuariosRepo.porId(usuarioId)
            ?: throw ApiError(HttpStatusCode.NotFound, "USUARIO_NO_ENCONTRADO", "Usuario no encontrado.")
        val fecha = runCatching { LocalDate.parse(request.fecha) }.getOrElse {
            throw ApiError(HttpStatusCode.UnprocessableEntity, "FECHA_INVALIDA", "La fecha debe usar formato ISO (YYYY-MM-DD).")
        }
        if (!Reglas.anticipacionSuficiente(fecha)) {
            throw ApiError(
                HttpStatusCode.UnprocessableEntity, "ANTICIPACION_INSUFICIENTE",
                "El bloqueo debe solicitarse con al menos ${Reglas.MINUTOS_ANTICIPACION_BLOQUEO} días de anticipación."
            )
        }
        if (ServiciosRepo.tieneAsignacion(usuarioId, fecha.toString())) {
            throw ApiError(HttpStatusCode.Conflict, "DIA_CON_SERVICIO", "No puedes bloquear un día con servicios asignados.")
        }
        if (DisponibilidadRepo.existe(usuarioId, fecha.toString())) {
            throw ApiError(HttpStatusCode.Conflict, "YA_BLOQUEADO", "El día ya está bloqueado.")
        }
        val bloqueo = DisponibilidadRepo.crear(
            nuevoId(), usuarioId, fecha.toString(), request.motivo?.trim()?.take(200)
        )
        call.respond(HttpStatusCode.Created, bloqueo)
    }

    delete("/disponibilidad/{id}") {
        val principal = call.exigirRol()
        val bloqueo = DisponibilidadRepo.buscar(call.idParametro())
            ?: throw ApiError(HttpStatusCode.NotFound, "BLOQUEO_NO_ENCONTRADO", "Bloqueo no encontrado.")
        if (principal.rol != RolApi.COORDINACION && bloqueo.usuarioId != principal.id) {
            throw ApiError(HttpStatusCode.Forbidden, "SIN_PERMISO", "No puedes eliminar bloqueos de otro usuario.")
        }
        DisponibilidadRepo.borrar(bloqueo.id)
        call.respond(HttpStatusCode.NoContent)
    }
}

private fun Route.rutaIncidencias() {
    get("/incidencias") {
        val principal = call.exigirRol()
        val usuarioId = if (principal.rol == RolApi.COORDINACION) {
            call.request.queryParameters["usuarioId"]
        } else {
            principal.id
        }
        call.respond(IncidenciasRepo.listar(call.request.queryParameters["servicioId"], usuarioId))
    }

    get("/incidencias/{id}") {
        val principal = call.exigirRol()
        val incidencia = IncidenciasRepo.buscar(call.idParametro())
            ?: throw ApiError(HttpStatusCode.NotFound, "INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada.")
        if (principal.rol != RolApi.COORDINACION && incidencia.usuarioId != principal.id) {
            throw ApiError(HttpStatusCode.Forbidden, "SIN_PERMISO", "No puedes ver incidencias de otro usuario.")
        }
        call.respond(incidencia)
    }

    post("/incidencias") {
        val principal = call.exigirRol()
        val request = call.receive<IncidenciaRequest>()
        ServiciosRepo.buscar(request.servicioId)
            ?: throw ApiError(HttpStatusCode.NotFound, "SERVICIO_NO_ENCONTRADO", "Servicio no encontrado.")
        if (request.categoria.trim().length !in 1..60) {
            throw ApiError(HttpStatusCode.UnprocessableEntity, "CATEGORIA_INVALIDA", "La categoría debe tener entre 1 y 60 caracteres.")
        }
        if (request.descripcion.trim().length !in 1..500) {
            throw ApiError(HttpStatusCode.UnprocessableEntity, "DESCRIPCION_INVALIDA", "La descripción debe tener entre 1 y 500 caracteres.")
        }
        if ((request.lat == null) != (request.lng == null)) {
            throw ApiError(HttpStatusCode.UnprocessableEntity, "COORDENADAS_INCOMPLETAS", "Latitud y longitud deben enviarse juntas.")
        }
        validarCoordenadas(request.lat, request.lng, false)
        val ocurridoEn = request.ocurridoEn?.let { Reglas.parsearInstante(it).toString() } ?: Instant.now().toString()
        call.respond(
            HttpStatusCode.Created,
            IncidenciasRepo.crear(nuevoId(), request, principal.id, ocurridoEn)
        )
    }

    put("/incidencias/{id}") {
        call.exigirRol(RolApi.COORDINACION)
        val id = call.idParametro()
        IncidenciasRepo.buscar(id)
            ?: throw ApiError(HttpStatusCode.NotFound, "INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada.")
        val request = call.receive<ActualizarIncidenciaRequest>()
        if (request.estado.uppercase() !in ESTADOS_INCIDENCIA) {
            throw ApiError(HttpStatusCode.UnprocessableEntity, "ESTADO_INVALIDO", "Estado debe ser ABIERTA, EN_PROCESO o CERRADA.")
        }
        call.respond(IncidenciasRepo.actualizar(id, request.estado)!!)
    }

    delete("/incidencias/{id}") {
        call.exigirRol(RolApi.COORDINACION)
        if (!IncidenciasRepo.borrar(call.idParametro())) {
            throw ApiError(HttpStatusCode.NotFound, "INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada.")
        }
        call.respond(HttpStatusCode.NoContent)
    }
}

private fun Route.rutaUsuarios() {
    get("/usuarios") {
        call.exigirRol(RolApi.COORDINACION)
        call.respond(cl.apiavengers.lince.backend.repositorios.UsuariosRepo.listar().map { it.aDtoUsuario() })
    }

    get("/usuarios/{id}") {
        call.exigirRol(RolApi.COORDINACION)
        val usuario = cl.apiavengers.lince.backend.repositorios.UsuariosRepo.porId(call.idParametro())
            ?: throw ApiError(HttpStatusCode.NotFound, "USUARIO_NO_ENCONTRADO", "Usuario no encontrado.")
        call.respond(usuario.aDtoUsuario())
    }

    post("/usuarios") {
        call.exigirRol(RolApi.COORDINACION)
        val request = call.receive<CrearUsuarioRequest>()
        if (request.email.isBlank() || request.password.isBlank() || request.nombre.isBlank()) {
            throw ApiError(HttpStatusCode.UnprocessableEntity, "SOLICITUD_INVALIDA", "Email, nombre y contraseña son obligatorios.")
        }
        val rol = runCatching { RolApi.valueOf(request.rol.uppercase()) }.getOrElse {
            throw ApiError(HttpStatusCode.UnprocessableEntity, "ROL_INVALIDO", "Rol inválido.")
        }
        val creado = cl.apiavengers.lince.backend.repositorios.UsuariosRepo.crear(
            request.email, request.nombre, rol, request.password
        )
        call.respond(HttpStatusCode.Created, creado.aDtoUsuario())
    }

    put("/usuarios/{id}") {
        call.exigirRol(RolApi.COORDINACION)
        val id = call.idParametro()
        val request = call.receive<ActualizarUsuarioRequest>()
        val rol = request.rol?.let { runCatching { RolApi.valueOf(it.uppercase()) }.getOrNull() }
        if (request.rol != null && rol == null) {
            throw ApiError(HttpStatusCode.UnprocessableEntity, "ROL_INVALIDO", "Rol inválido.")
        }
        val actualizado = cl.apiavengers.lince.backend.repositorios.UsuariosRepo.actualizar(
            id, request.email, request.nombre, rol, request.password
        ) ?: throw ApiError(HttpStatusCode.NotFound, "USUARIO_NO_ENCONTRADO", "Usuario no encontrado.")
        call.respond(actualizado.aDtoUsuario())
    }

    delete("/usuarios/{id}") {
        call.exigirRol(RolApi.COORDINACION)
        if (!cl.apiavengers.lince.backend.repositorios.UsuariosRepo.borrar(call.idParametro())) {
            throw ApiError(HttpStatusCode.NotFound, "USUARIO_NO_ENCONTRADO", "Usuario no encontrado.")
        }
        call.respond(HttpStatusCode.NoContent)
    }
}

private fun Route.rutaCoordinacion() {
    get("/coordinacion/servicios/{servicioId}/ultima-posicion") {
        call.exigirRol(RolApi.COORDINACION)
        val servicioId = call.parameters["servicioId"].orEmpty()
        ServiciosRepo.buscar(servicioId)
            ?: throw ApiError(HttpStatusCode.NotFound, "SERVICIO_NO_ENCONTRADO", "Servicio no encontrado.")
        val posicion = HitosRepo.ultimaPosicion(servicioId)
            ?: throw ApiError(HttpStatusCode.NotFound, "SIN_POSICION", "No hay posición reportada en un servicio activo.")
        call.respond(posicion)
    }
}

private val ESTADOS_INCIDENCIA = setOf("ABIERTA", "EN_PROCESO", "CERRADA")

private fun ApplicationCall.exigirRol(vararg roles: RolApi): UsuarioPrincipal {
    val principal = principal<UsuarioPrincipal>()
        ?: throw ApiError(HttpStatusCode.Unauthorized, "NO_AUTENTICADO", "Debes iniciar sesión.")
    if (roles.isNotEmpty() && principal.rol !in roles) {
        throw ApiError(HttpStatusCode.Forbidden, "SIN_PERMISO", "Tu rol no permite esta operación.")
    }
    return principal
}

private fun ApplicationCall.idParametro(): String =
    parameters["id"]?.takeIf { it.isNotBlank() }
        ?: throw ApiError(HttpStatusCode.BadRequest, "ID_REQUERIDO", "Falta el identificador.")

private fun parsearTipo(valor: String): TipoHito =
    runCatching { TipoHito.valueOf(valor.trim().uppercase()) }.getOrElse {
        throw ApiError(
            HttpStatusCode.UnprocessableEntity, "TIPO_HITO_INVALIDO",
            "Tipo debe ser CHECK_IN, CHECK_OUT, CONFIRMACION, RECHAZO o REPORTE_POSICION."
        )
    }

private fun validarCoordenadas(lat: Double?, lng: Double?, obligatorias: Boolean) {
    if (lat == null || lng == null) {
        if (obligatorias) {
            throw ApiError(HttpStatusCode.UnprocessableEntity, "COORDENADAS_REQUERIDAS", "Latitud y longitud son obligatorias.")
        }
        return
    }
    if (lat !in -90.0..90.0) {
        throw ApiError(HttpStatusCode.UnprocessableEntity, "LATITUD_INVALIDA", "Latitud fuera de rango.")
    }
    if (lng !in -180.0..180.0) {
        throw ApiError(HttpStatusCode.UnprocessableEntity, "LONGITUD_INVALIDA", "Longitud fuera de rango.")
    }
}

private fun validarGeocerca(request: GeocercaRequest) {
    if (request.tipo.uppercase() !in setOf("HOTEL", "PICKUP")) {
        throw ApiError(HttpStatusCode.UnprocessableEntity, "TIPO_GEOCERCA_INVALIDO", "Tipo debe ser HOTEL o PICKUP.")
    }
    if (request.nombre.trim().length !in 1..120) {
        throw ApiError(HttpStatusCode.UnprocessableEntity, "NOMBRE_INVALIDO", "El nombre debe tener entre 1 y 120 caracteres.")
    }
    if (request.radioM !in Reglas.RADIO_MINIMO_M..Reglas.RADIO_MAXIMO_M) {
        throw ApiError(
            HttpStatusCode.UnprocessableEntity, "RADIO_INVALIDO",
            "El radio debe estar entre ${Reglas.RADIO_MINIMO_M} y ${Reglas.RADIO_MAXIMO_M} metros."
        )
    }
    validarCoordenadas(request.lat, request.lng, true)
}

private fun UsuarioFila.aDto() = UsuarioDto(id, email, nombre, rol.name)
