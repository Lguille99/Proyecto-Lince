package cl.apiavengers.lince.backend

object Config {
    val puerto: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val host: String = System.getenv("HOST") ?: "0.0.0.0"
    val rutaDb: String = System.getenv("DB_PATH") ?: "data/lince.db"
    val jwtSecreto: String = System.getenv("JWT_SECRET") ?: "dev-lince-secret-cambiar"
    val jwtEmisor: String = System.getenv("JWT_ISSUER") ?: "lince-backend"
    val jwtAudiencia: String = System.getenv("JWT_AUDIENCE") ?: "lince-app"
    val duracionTokenMinutos: Long = System.getenv("JWT_TTL_MINUTES")?.toLongOrNull() ?: 480
    val passwordDemo: String = System.getenv("DEMO_PASSWORD") ?: "Lince123"
    val southboundBaseUrl: String = System.getenv("SOUTHBOUND_BASE_URL").orEmpty()
    val southboundApiKey: String = System.getenv("SOUTHBOUND_API_KEY").orEmpty()
}
