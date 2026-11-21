package cl.apiavengers.lince.backend

import cl.apiavengers.lince.backend.db.Db
import cl.apiavengers.lince.backend.modelo.ErrorRespuesta
import cl.apiavengers.lince.backend.modelo.SaludRespuesta
import cl.apiavengers.lince.backend.rutas.ApiError
import cl.apiavengers.lince.backend.rutas.rutasProtegidas
import cl.apiavengers.lince.backend.rutas.rutasPublicas
import cl.apiavengers.lince.backend.seguridad.Jwt
import cl.apiavengers.lince.backend.seguridad.RolApi
import cl.apiavengers.lince.backend.seguridad.UsuarioPrincipal
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.util.UUID
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun main() {
    Db.inicializar()
    embeddedServer(Netty, port = Config.puerto, host = Config.host) { modulo() }
        .start(wait = true)
}

fun Application.modulo() {
    Db.inicializar()

    install(DefaultHeaders)
    install(CallId) {
        generate { UUID.randomUUID().toString() }
        replyToHeader("X-Request-Id")
    }
    install(CallLogging) {
        level = Level.INFO
        format { llamada ->
            "${llamada.request.httpMethod.value} ${llamada.request.path()} " +
                "requestId=${llamada.callId}"
        }
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-Request-Id")
        allowHeader("X-API-Key")
        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        })
    }
    install(StatusPages) {
        exception<ApiError> { llamada, causa ->
            llamada.respond(
                causa.status,
                ErrorRespuesta(causa.codigo, causa.message ?: "Error", llamada.callId)
            )
        }
        exception<kotlinx.serialization.SerializationException> { llamada, causa ->
            llamada.respond(
                HttpStatusCode.BadRequest,
                ErrorRespuesta("JSON_INVALIDO", causa.message ?: "Cuerpo JSON inválido.", llamada.callId)
            )
        }
        exception<Throwable> { llamada, causa ->
            this@modulo.log.error("Error no controlado requestId=${llamada.callId}", causa)
            llamada.respond(
                HttpStatusCode.InternalServerError,
                ErrorRespuesta("ERROR_INTERNO", "Error interno del servidor.", llamada.callId)
            )
        }
    }
    install(Authentication) {
        jwt("jwt") {
            verifier(Jwt.verificador())
            validate { credencial ->
                val rolTexto = credencial.payload.getClaim("rol").asString()
                val rol = rolTexto?.let { runCatching { RolApi.valueOf(it) }.getOrNull() }
                if (rol == null) return@validate null
                UsuarioPrincipal(
                    id = credencial.payload.subject,
                    email = credencial.payload.getClaim("email").asString().orEmpty(),
                    nombre = credencial.payload.getClaim("nombre").asString().orEmpty(),
                    rol = rol
                )
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorRespuesta("NO_AUTENTICADO", "Token ausente o inválido.", call.callId)
                )
            }
        }
    }

    routing {
        get("/health") { call.respond(SaludRespuesta("ok", "0.1.0")) }
        get("/openapi.yaml") {
            val recurso = this@modulo.javaClass.classLoader.getResourceAsStream("openapi.yaml")
                ?: throw ApiError(HttpStatusCode.NotFound, "OPENAPI_NO_DISPONIBLE", "openapi.yaml no encontrado.")
            call.respondText(recurso.bufferedReader().use { it.readText() }, ContentType.parse("application/yaml"))
        }
        get("/docs") {
            call.respondText(htmlSwagger(), ContentType.Text.Html)
        }
        rutasPublicas()
        authenticate("jwt", optional = false) {
            rutasProtegidas()
        }
    }
}

private fun htmlSwagger(): String = """
    <!doctype html>
    <html lang="es">
    <head>
      <meta charset="utf-8"/>
      <meta name="viewport" content="width=device-width,initial-scale=1"/>
      <title>Lince API · Swagger</title>
      <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css"/>
    </head>
    <body>
    <div id="swagger-ui"></div>
    <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
    <script>
      window.ui = SwaggerUIBundle({ url: '/openapi.yaml', dom_id: '#swagger-ui' });
    </script>
    </body>
    </html>
""".trimIndent()
