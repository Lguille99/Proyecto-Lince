package cl.apiavengers.lince.datos.local

import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class TipoOperacionSync {
    CONFIRMAR,
    RECHAZAR,
    CHECK_IN,
    CHECK_OUT,
    REPORTAR_UBICACION,
    CALIFICAR,
    REASIGNAR,
    PERFIL,
    DISPONIBILIDAD
}

data class OperacionPendiente(
    val tipo: TipoOperacionSync,
    val servicioId: String? = null,
    val detalle: String = ""
)

interface Sincronizador {
    suspend fun enviar(operacion: OperacionPendienteEntity): Boolean
}

class SincronizadorLocal(private val pausaMs: Long = 120) : Sincronizador {
    override suspend fun enviar(operacion: OperacionPendienteEntity): Boolean {
        delay(pausaMs)
        return true
    }
}

class ColaSincronizacion(private val dao: OperacionPendienteDao) {
    fun encolar(operacion: OperacionPendiente): Long {
        val id = dao.insertar(
            OperacionPendienteEntity(
                tipo = operacion.tipo.name,
                servicioId = operacion.servicioId,
                detalle = operacion.detalle,
                creadoEn = LocalDateTime.now().withNano(0).toString(),
                estado = EstadoOperacion.PENDIENTE.name
            )
        )
        actualizarPendientes()
        return id
    }

    fun pendientes(): List<OperacionPendienteEntity> =
        dao.consultarPorEstado(EstadoOperacion.PENDIENTE.name)

    fun contar(): Int = dao.contarPorEstado(EstadoOperacion.PENDIENTE.name)

    fun actualizarPendientes() {
        _pendientes.value = contar()
    }

    suspend fun sincronizar(sincronizador: Sincronizador): Int = candado.withLock {
        reintentarErrores()
        var enviadas = 0
        pendientes().forEach { operacion ->
            val exito = runCatching { sincronizador.enviar(operacion) }.getOrDefault(false)
            if (exito) {
                dao.actualizarEstado(operacion.id, EstadoOperacion.SINCRONIZADA.name)
                enviadas++
            } else {
                dao.actualizarEstado(operacion.id, EstadoOperacion.ERROR.name)
                dao.registrarIntento(operacion.id)
            }
        }
        actualizarPendientes()
        enviadas
    }

    fun limpiarSincronizadas() {
        dao.borrarPorEstado(EstadoOperacion.SINCRONIZADA.name)
        actualizarPendientes()
    }

    fun reintentarErrores() {
        dao.consultarPorEstado(EstadoOperacion.ERROR.name).forEach { operacion ->
            dao.actualizarEstado(operacion.id, EstadoOperacion.PENDIENTE.name)
        }
        actualizarPendientes()
    }

    companion object {
        private val candado = Mutex()
        private val _pendientes = MutableStateFlow(0)
        val pendientes: StateFlow<Int> = _pendientes.asStateFlow()
    }
}
