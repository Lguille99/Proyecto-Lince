package cl.apiavengers.lince.backend.seguridad

import cl.apiavengers.lince.backend.Config
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

enum class RolApi { GUIA, TRANSPORTISTA, COORDINACION }

data class UsuarioPrincipal(
    val id: String,
    val email: String,
    val nombre: String,
    val rol: RolApi
)

object Jwt {
    private val algoritmo = Algorithm.HMAC256(Config.jwtSecreto)
    private val verificador: JWTVerifier = JWT.require(algoritmo)
        .withIssuer(Config.jwtEmisor)
        .withAudience(Config.jwtAudiencia)
        .build()

    fun verificador(): JWTVerifier = verificador

    fun emitir(usuario: UsuarioPrincipal): String {
        val ahora = Date()
        val expira = Date(ahora.time + Config.duracionTokenMinutos * 60_000)
        return JWT.create()
            .withIssuer(Config.jwtEmisor)
            .withAudience(Config.jwtAudiencia)
            .withSubject(usuario.id)
            .withClaim("email", usuario.email)
            .withClaim("nombre", usuario.nombre)
            .withClaim("rol", usuario.rol.name)
            .withIssuedAt(ahora)
            .withExpiresAt(expira)
            .sign(algoritmo)
    }

    fun expiracion(): String =
        Date(System.currentTimeMillis() + Config.duracionTokenMinutos * 60_000).toInstant().toString()
}
