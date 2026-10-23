package cl.apiavengers.lince.modelo

data class LinceUiState(
    val usuario: Usuario? = null,
    val servicios: List<Servicio> = emptyList(),
    val historial: List<Servicio> = emptyList(),
    val equipo: List<Usuario> = emptyList(),
    val resumen: ResumenCoordinador? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)
