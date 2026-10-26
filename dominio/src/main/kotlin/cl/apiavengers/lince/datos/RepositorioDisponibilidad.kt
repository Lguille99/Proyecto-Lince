package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.modelo.Servicio
import cl.apiavengers.lince.modelo.Usuario
import java.time.Clock
import java.time.LocalDate

class RepositorioDisponibilidad(
    private val almacen: AlmacenDisponibilidad,
    private val reloj: Clock = Clock.systemDefaultZone()
) {
    fun consultar(usuario: Usuario?): Set<LocalDate> {
        val recurso = validarUsuario(usuario)
        return almacen.leer(recurso.cuenta)
    }

    fun guardar(
        usuario: Usuario?,
        fecha: LocalDate,
        bloqueado: Boolean,
        serviciosAsignados: List<Servicio> = emptyList()
    ): Set<LocalDate> {
        val recurso = validarUsuario(usuario)
        val hoy = LocalDate.now(reloj)
        require(!fecha.isBefore(hoy)) { "Selecciona hoy o una fecha futura." }
        if (bloqueado) {
            require(!fecha.isBefore(hoy.plusDays(7))) {
                "Debes avisar con al menos 7 días de anticipación."
            }
            val ocupado = serviciosAsignados.any {
                it.fecha == fecha.toString() &&
                    it.esDe(recurso.id) &&
                    it.estado != EstadoServicio.RECHAZADA &&
                    it.estado != EstadoServicio.COMPLETADA
            }
            require(!ocupado) { "No puedes bloquear un día con un servicio asignado." }
        }
        val fechas = almacen.leer(recurso.cuenta)
        val actualizadas = if (bloqueado) fechas + fecha else fechas - fecha
        almacen.guardar(recurso.cuenta, actualizadas)
        return actualizadas
    }

    private fun validarUsuario(usuario: Usuario?): Usuario {
        requireNotNull(usuario) { "Debes iniciar sesión." }
        require(usuario.rol != Rol.COORDINADOR) { "La agenda personal corresponde a conductores y guías." }
        require(usuario.cuenta.isNotBlank()) { "La cuenta no es válida." }
        return usuario
    }
}
