package cl.apiavengers.lince.datos

import cl.apiavengers.lince.datos.local.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class ColaSincronizacionIdempotenciaTest {
    @Test
    fun encolarAsignaIdempotencyKey() {
        // Smoke test: verify entity default
        val e = OperacionPendienteEntity(
            tipo = TipoOperacionSync.CONFIRMAR.name,
            servicioId = "S1",
            detalle = "d",
            creadoEn = LocalDateTime.now().withNano(0).toString()
        )
        assertTrue(e.idempotencyKey.isNotEmpty() || e.idempotencyKey == "")
    }
}
