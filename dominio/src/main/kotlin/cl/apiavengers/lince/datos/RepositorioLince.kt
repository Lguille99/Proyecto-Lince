package cl.apiavengers.lince.datos

import cl.apiavengers.lince.modelo.*
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class RepositorioLince(
    private val almacen: AlmacenEstado = AlmacenEstadoMemoria(),
    private val reloj: Clock = Clock.systemDefaultZone()
) {
    private var guardado = almacen.leer()
    private var usuarioActual: Usuario? = null

    private val hoy: LocalDate = LocalDate.now(reloj)
    private val baseServicios: List<Servicio> = DatosPrueba.servicios(hoy) + DatosPrueba.historial(hoy)
    private val baseUsuarios: List<Usuario> = DatosPrueba.usuarios

    fun iniciarSesion(cuenta: String, clave: String): Usuario {
        usuarioActual = null
        val usuario = usuarios().find { it.cuenta == cuenta.trim() }
        require(usuario != null && clave == DatosPrueba.CLAVE_DEMO) {
            "Usuario o contraseña incorrectos."
        }
        usuarioActual = usuario
        return usuario
    }

    fun cerrarSesion() {
        usuarioActual = null
    }

    fun consultarServicios(): List<Servicio> {
        val usuario = exigirSesion()
        val activos = activos()
        return if (usuario.rol == Rol.COORDINADOR) activos
        else activos.filter { it.esDe(usuario.id) }
    }

    fun consultarHistorial(): List<Servicio> {
        val usuario = exigirSesion()
        val historial = historial()
        return if (usuario.rol == Rol.COORDINADOR) historial
        else historial.filter { it.esDe(usuario.id) }
    }

    fun consultarHistorialDe(usuarioId: String): List<Servicio> {
        exigirSesion()
        return historial().filter { it.esDe(usuarioId) }
    }

    fun consultarServiciosDe(usuarioId: String): List<Servicio> {
        exigirSesion()
        return activos().filter { it.esDe(usuarioId) }
    }

    fun consultarAgendaDelDia(): List<Servicio> {
        val usuario = exigirSesion()
        val deHoy = activos().filter { it.fecha == hoy.toString() }
        return if (usuario.rol == Rol.COORDINADOR) deHoy
        else deHoy.filter { it.esDe(usuario.id) }
    }

    fun resumenCoordinador(): ResumenCoordinador {
        val usuario = exigirSesion()
        require(usuario.rol == Rol.COORDINADOR) { "Resumen disponible solo para coordinadores." }
        val activos = activos()
        val historial = historial()
        val personas = usuarios()
        return ResumenCoordinador(
            serviciosActivos = activos.size,
            pendientes = activos.count { it.estado == EstadoServicio.PENDIENTE },
            confirmadas = activos.count { it.estado == EstadoServicio.CONFIRMADA },
            rechazadas = historial.count { it.estado == EstadoServicio.RECHAZADA },
            enEjecucion = activos.count { it.inicioEn != null && it.terminoEn == null },
            personasEquipo = personas.size,
            personasDisponibles = personas.count { it.disponibilidad == Disponibilidad.DISPONIBLE },
            historialTotal = historial.size
        )
    }

    fun consultarEquipo(): List<Usuario> {
        exigirSesion()
        return usuarios()
    }

    fun obtenerUsuarioPorId(id: String): Usuario {
        exigirSesion()
        return usuarios().find { it.id == id }
            ?: throw IllegalArgumentException("Persona no encontrada: $id")
    }

    fun obtenerServicioPorId(id: String): Servicio {
        exigirSesion()
        return todos().find { it.id == id }
            ?: throw IllegalArgumentException("Servicio no encontrado: $id")
    }

    fun confirmarServicio(servicioId: String) {
        val usuario = exigirSesion()
        val servicio = servicioInterno(servicioId)
        require(servicio.esTitular(usuario.id)) { "Solo el titular puede responder esta asignación." }
        require(servicio.estado == EstadoServicio.PENDIENTE) { "La asignación ya fue respondida." }
        persistir(
            servicio.copy(
                estado = EstadoServicio.CONFIRMADA,
                respondidoPor = usuario.id,
                respondidoEn = ahora(),
                motivoRechazo = null
            )
        )
    }

    fun rechazarServicio(servicioId: String, motivo: String) {
        val usuario = exigirSesion()
        val servicio = servicioInterno(servicioId)
        val limpio = motivo.trim()
        require(servicio.esTitular(usuario.id)) { "Solo el titular puede responder esta asignación." }
        require(servicio.estado == EstadoServicio.PENDIENTE) { "La asignación ya fue respondida." }
        require(limpio.length in 1..300) { "Indica un motivo de entre 1 y 300 caracteres." }
        persistir(
            servicio.copy(
                estado = EstadoServicio.RECHAZADA,
                respondidoPor = usuario.id,
                respondidoEn = ahora(),
                motivoRechazo = limpio
            )
        )
    }

    fun registrarCheckIn(servicioId: String, ubicacion: String, observaciones: String = "") {
        val usuario = exigirSesion()
        require(usuario.rol != Rol.COORDINADOR) { "Solo el personal asignado registra la ejecución." }
        val servicio = servicioInterno(servicioId)
        require(servicio.esTitular(usuario.id)) { "Solo el titular registra la ejecución." }
        require(servicio.estado == EstadoServicio.CONFIRMADA) { "El servicio debe estar confirmado." }
        require(servicio.inicioEn == null) { "El servicio ya tiene un check-in registrado." }
        val lugar = ubicacion.trim()
        require(lugar.isNotEmpty()) { "Indica la ubicación donde comienza el servicio." }
        val ahora = LocalDateTime.now(reloj).withNano(0)
        val limite = servicio.inicioProgramado.minusMinutes(15)
        require(!ahora.isBefore(limite)) {
            "El check-in se habilita 15 minutos antes de la hora asignada."
        }
        persistir(
            servicio.copy(
                inicioEn = ahora.toString(),
                inicioPor = usuario.id,
                ubicacionInicio = lugar,
                observaciones = observaciones.trim().take(500)
            )
        )
    }

    fun registrarCheckOut(servicioId: String, observaciones: String = "") {
        val usuario = exigirSesion()
        require(usuario.rol != Rol.COORDINADOR) { "Solo el personal asignado registra la ejecución." }
        val servicio = servicioInterno(servicioId)
        require(servicio.esTitular(usuario.id)) { "Solo el titular registra la ejecución." }
        require(servicio.estado == EstadoServicio.CONFIRMADA) { "El servicio debe estar confirmado." }
        require(servicio.inicioEn != null) { "Primero debes registrar el inicio." }
        require(servicio.terminoEn == null) { "El servicio ya tiene un check-out registrado." }
        val ahora = LocalDateTime.now(reloj).withNano(0)
        val inicio = parsear(servicio.inicioEn) ?: ahora
        require(!ahora.isBefore(inicio)) { "El término no puede ser anterior al inicio." }
        persistir(
            servicio.copy(
                estado = EstadoServicio.COMPLETADA,
                terminoEn = ahora.toString(),
                observaciones = if (observaciones.isBlank()) servicio.observaciones else observaciones.trim().take(500)
            )
        )
    }

    fun calificarServicio(servicioId: String, puntaje: Int) {
        val usuario = exigirSesion()
        require(usuario.rol == Rol.COORDINADOR) { "Solo coordinación califica los servicios." }
        val servicio = servicioInterno(servicioId)
        require(servicio.estado == EstadoServicio.COMPLETADA) { "Solo se califican servicios completados." }
        require(puntaje in 1..5) { "La calificación debe estar entre 1 y 5." }
        persistir(
            servicio.copy(
                puntaje = puntaje,
                calificadoPor = usuario.id,
                calificadoEn = ahora()
            )
        )
    }

    fun reasignarServicio(servicioId: String, nuevoUsuarioId: String) {
        val usuario = exigirSesion()
        require(usuario.rol == Rol.COORDINADOR) { "Solo coordinación reasigna servicios." }
        val servicio = servicioInterno(servicioId)
        val actual = usuarios().find { it.id == servicio.asignadoA }
            ?: throw IllegalArgumentException("Titular actual no encontrado.")
        val candidato = usuarios().find { it.id == nuevoUsuarioId }
            ?: throw IllegalArgumentException("La persona seleccionada no existe.")
        require(candidato.id != servicio.asignadoA && candidato.id != servicio.acompananteId) {
            "La persona ya está asignada a este servicio."
        }
        require(candidato.rol == actual.rol) { "El reemplazo debe tener el mismo rol que el titular." }
        require(servicio.idiomaRequerido in candidato.idiomas) {
            "La persona no maneja el idioma requerido (${servicio.idiomaRequerido.etiqueta})."
        }
        persistir(
            servicio.copy(
                asignadoA = candidato.id,
                estado = EstadoServicio.PENDIENTE,
                respondidoPor = null,
                respondidoEn = null,
                motivoRechazo = null,
                inicioEn = null,
                terminoEn = null,
                inicioPor = null,
                ubicacionInicio = null,
                observaciones = "",
                puntaje = null,
                calificadoPor = null,
                calificadoEn = null,
                reasignadoPor = usuario.id,
                reasignadoEn = ahora()
            )
        )
    }

    fun candidatosReasignacion(servicioId: String): List<Usuario> {
        exigirSesion()
        val servicio = servicioInterno(servicioId)
        val actual = usuarios().find { it.id == servicio.asignadoA } ?: return emptyList()
        return usuarios().filter {
            it.rol == actual.rol &&
                it.id != servicio.asignadoA &&
                it.id != servicio.acompananteId &&
                servicio.idiomaRequerido in it.idiomas
        }
    }

    fun actualizarPerfil(
        nombre: String,
        correo: String,
        telefono: String,
        zona: String,
        idiomas: List<Idioma>
    ): Usuario {
        val usuario = exigirSesion()
        val nombreLimpio = nombre.trim()
        val correoLimpio = correo.trim()
        val telefonoLimpio = telefono.trim()
        val zonaLimpia = zona.trim()
        require(nombreLimpio.length in 2..60) { "El nombre debe tener entre 2 y 60 caracteres." }
        require(correoLimpio.contains("@") && correoLimpio.substringAfter("@").contains(".")) {
            "Ingresa un correo válido."
        }
        require(telefonoLimpio.length >= 8) { "Ingresa un teléfono válido." }
        require(zonaLimpia.isNotEmpty()) { "Indica tu zona de trabajo." }
        require(idiomas.isNotEmpty()) { "Selecciona al menos un idioma." }
        val cambio = CambioPerfil(nombreLimpio, correoLimpio, telefonoLimpio, zonaLimpia, idiomas)
        guardado = guardado.copy(perfiles = guardado.perfiles + (usuario.id to cambio))
        almacen.guardar(guardado)
        val actualizado = usuario.copy(
            nombre = nombreLimpio,
            correo = correoLimpio,
            telefono = telefonoLimpio,
            zona = zonaLimpia,
            idiomas = idiomas
        )
        usuarioActual = actualizado
        return actualizado
    }

    fun totalGanado(usuarioId: String): Int = todos()
        .filter {
            it.esDe(usuarioId) &&
                (it.estado == EstadoServicio.CONFIRMADA || it.estado == EstadoServicio.COMPLETADA)
        }
        .sumOf { it.tarifa }

    fun promedioCalificacion(usuarioId: String): Double? {
        val puntajes = todos().filter { it.esDe(usuarioId) }.mapNotNull { it.puntaje }
        if (puntajes.isEmpty()) return null
        return puntajes.average()
    }

    fun serviciosDelDiaPara(usuarioId: String): List<Servicio> = activos()
        .filter { it.fecha == hoy.toString() && it.esDe(usuarioId) }

    private fun activos(): List<Servicio> = todos().filter {
        it.estado == EstadoServicio.PENDIENTE || it.estado == EstadoServicio.CONFIRMADA
    }

    private fun historial(): List<Servicio> = todos().filter {
        it.estado == EstadoServicio.COMPLETADA || it.estado == EstadoServicio.RECHAZADA
    }

    private fun todos(): List<Servicio> = baseServicios.map(::aplicarCambios)

    private fun aplicarCambios(servicio: Servicio): Servicio {
        val cambio = guardado.servicios[servicio.id] ?: return servicio
        return servicio.copy(
            estado = cambio.estado,
            asignadoA = cambio.asignadoA,
            acompananteId = cambio.acompananteId,
            respondidoPor = cambio.respondidoPor,
            respondidoEn = cambio.respondidoEn,
            motivoRechazo = cambio.motivoRechazo,
            inicioEn = cambio.inicioEn,
            terminoEn = cambio.terminoEn,
            inicioPor = cambio.inicioPor,
            ubicacionInicio = cambio.ubicacionInicio,
            observaciones = cambio.observaciones,
            puntaje = cambio.puntaje,
            calificadoPor = cambio.calificadoPor,
            calificadoEn = cambio.calificadoEn,
            reasignadoPor = cambio.reasignadoPor,
            reasignadoEn = cambio.reasignadoEn
        )
    }

    private fun usuarios(): List<Usuario> = baseUsuarios.map { usuario ->
        val cambio = guardado.perfiles[usuario.id] ?: return@map usuario
        usuario.copy(
            nombre = cambio.nombre,
            correo = cambio.correo,
            telefono = cambio.telefono,
            zona = cambio.zona,
            idiomas = cambio.idiomas
        )
    }

    private fun servicioInterno(id: String): Servicio = todos().find { it.id == id }
        ?: throw IllegalArgumentException("Servicio no encontrado: $id")

    private fun persistir(servicio: Servicio) {
        val cambio = CambioServicio(
            estado = servicio.estado,
            asignadoA = servicio.asignadoA,
            acompananteId = servicio.acompananteId,
            respondidoPor = servicio.respondidoPor,
            respondidoEn = servicio.respondidoEn,
            motivoRechazo = servicio.motivoRechazo,
            inicioEn = servicio.inicioEn,
            terminoEn = servicio.terminoEn,
            inicioPor = servicio.inicioPor,
            ubicacionInicio = servicio.ubicacionInicio,
            observaciones = servicio.observaciones,
            puntaje = servicio.puntaje,
            calificadoPor = servicio.calificadoPor,
            calificadoEn = servicio.calificadoEn,
            reasignadoPor = servicio.reasignadoPor,
            reasignadoEn = servicio.reasignadoEn
        )
        guardado = guardado.copy(servicios = guardado.servicios + (servicio.id to cambio))
        almacen.guardar(guardado)
    }

    private fun exigirSesion(): Usuario = usuarioActual
        ?: throw IllegalStateException("Debes iniciar sesión.")

    private fun ahora(): String = LocalDateTime.now(reloj).withNano(0).toString()

    private fun parsear(texto: String?): LocalDateTime? = try {
        texto?.let { LocalDateTime.parse(it) }
    } catch (_: DateTimeParseException) {
        null
    }
}
