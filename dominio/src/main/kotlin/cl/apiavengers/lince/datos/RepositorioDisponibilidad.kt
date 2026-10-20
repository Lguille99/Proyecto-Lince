package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.Rol
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

    fun guardar(usuario: Usuario?, fecha: LocalDate, bloqueado: Boolean): Set<LocalDate> {
        val recurso = validarUsuario(usuario)
        require(!fecha.isBefore(LocalDate.now(reloj))) { "Selecciona hoy o una fecha futura." }
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
