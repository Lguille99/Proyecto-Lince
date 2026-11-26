package cl.apiavengers.lince.modelo

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class DisponibilidadUiState(
    val fecha: LocalDate = LocalDate.now(),
    val bloqueado: Boolean = false,
    val diasBloqueados: Set<LocalDate> = emptySet(),
    val cargando: Boolean = true,
    val cargado: Boolean = false,
    val mensaje: String? = null,
    val esError: Boolean = false
)
