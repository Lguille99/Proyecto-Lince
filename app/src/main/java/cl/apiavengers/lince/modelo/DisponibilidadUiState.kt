package cl.apiavengers.lince.modelo

import java.time.LocalDate

data class DisponibilidadUiState(
    val fecha: LocalDate = LocalDate.now(),
    val bloqueado: Boolean = false,
    val diasBloqueados: Set<LocalDate> = emptySet(),
    val cargando: Boolean = true,
    val cargado: Boolean = false,
    val mensaje: String? = null,
    val esError: Boolean = false
)
