package cl.apiavengers.lince.ui.idioma

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import cl.apiavengers.lince.datos.preferencias.Preferencias
import cl.apiavengers.lince.datos.preferencias.PreferenciasDataStore
import cl.apiavengers.lince.modelo.Disponibilidad
import cl.apiavengers.lince.modelo.EstadoServicio
import cl.apiavengers.lince.modelo.Idioma
import cl.apiavengers.lince.modelo.Modalidad
import cl.apiavengers.lince.modelo.Rol
import cl.apiavengers.lince.ui.theme.ModoTema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

enum class IdiomaApp { ESPANOL, INGLES }

object PreferenciaIdioma {
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var almacen: PreferenciasDataStore? = null
    private var iniciado = false

    private val _idioma = MutableStateFlow(IdiomaApp.ESPANOL)
    val idioma: StateFlow<IdiomaApp> = _idioma.asStateFlow()

    fun iniciar(context: Context) {
        if (iniciado) return
        iniciado = true
        val preferencias = Preferencias.obtener(context)
        almacen = preferencias
        runCatching {
            _idioma.value = leer(runBlocking { preferencias.idioma.first() })
        }
        alcance.launch {
            preferencias.idioma.collect { valor -> _idioma.value = leer(valor) }
        }
    }

    fun fijar(nuevo: IdiomaApp) {
        _idioma.value = nuevo
        almacen?.let { preferencias ->
            alcance.launch { preferencias.fijarIdioma(nuevo.name) }
        }
    }

    fun actuales(): Textos = if (_idioma.value == IdiomaApp.INGLES) TextosIngles else TextosEspanol

    fun traducir(mensaje: String?): String? = actuales().traducir(mensaje)

    private fun leer(valor: String?): IdiomaApp =
        runCatching { valor?.let(IdiomaApp::valueOf) }.getOrNull() ?: IdiomaApp.ESPANOL
}

val LocalTextos = staticCompositionLocalOf<Textos> { TextosEspanol }

@Composable
fun textos(): Textos = LocalTextos.current

interface Textos {
    val appName: String
    val appTagline: String
    val back: String
    val logout: String
    val navHome: String
    val navServices: String
    val navTeam: String
    val navAgenda: String
    val navProfile: String
    val language: String
    val languageSpanish: String
    val languageEnglish: String
    val confirm: String
    val cancel: String
    val save: String
    val accept: String
    val all: String
    val viewAll: String
    val viewDetails: String
    val pending: String
    val unassigned: String
    val id: String

    fun modoTema(modo: ModoTema): String
    fun rol(rol: Rol): String
    fun disponibilidad(valor: Disponibilidad): String
    fun estado(valor: EstadoServicio): String
    fun idioma(valor: Idioma): String
    fun modalidad(valor: Modalidad): String

    val connectionOffline: String
    val connectionOnline: String
    fun pendingSync(cantidad: Int): String

    val loginLogoDescription: String
    val loginWelcome: String
    val loginSubtitle: String
    val loginUser: String
    val loginPassword: String
    val loginShowPassword: String
    val loginHidePassword: String
    val loginButton: String
    val loginDemoTitle: String
    val loginDemoHint: String
    val loginUse: String
    val loginFooter: String

    val profileTitle: String
    val profileId: String
    val profileName: String
    val profileEmail: String
    val profilePhone: String
    val profileZone: String
    val profileLanguages: String
    val profileSave: String
    val profileViewTeam: String
    val profileAppearance: String
    val profileAppearanceHint: String
    val profileLanguage: String
    val profileLanguageHint: String
    val profileDataLocal: String
    val errorName: String
    val errorEmail: String
    val errorPhone: String
    val errorZone: String
    val errorLanguages: String
    fun accumulated(monto: String): String
    fun averageRating(valor: String): String

    fun homeGreeting(nombre: String): String
    val homeUserFallback: String
    val homeDay: String
    fun homePending(cantidad: Int): String
    fun homeTodayAssigned(cantidad: Int): String
    val homeTodayServices: String
    val homeNextService: String
    val homeNoActive: String
    val homeNoActiveMessage: String
    val homeUpdateAgenda: String
    val homeViewServices: String
    val homeViewHistory: String
    val homeViewTeam: String
    val homeAvailability: String
    val homeAvailabilityHint: String

    val servicesTitle: String
    val servicesAllSubtitle: String
    val servicesMineSubtitle: String
    val servicesSearch: String
    val servicesAllFilter: String
    val servicesPendingFilter: String
    val servicesConfirmedFilter: String
    val servicesNoResults: String
    val servicesNoMatch: String
    val servicesClearFilters: String
    val today: String
    val lunchIncluded: String

    val historyTitle: String
    val historyGeneral: String
    val historyAccumulated: String
    fun historyRegistered(cantidad: Int): String
    val historyIncludes: String
    val historyConfirmedCompleted: String
    val historyCompletedFilter: String
    val historyRejectedFilter: String
    val historyEmpty: String
    val historyEmptyMessage: String
    fun responsible(nombre: String): String
    fun rejectedAt(valor: String): String
    fun confirmedAt(valor: String): String
    fun rating(value: String): String
    val fare: String

    val executionTitle: String
    val serviceNotFound: String
    val executionMilestones: String
    fun observations(valor: String): String
    val responsibleTitle: String
    val viewHolderProfile: String
    fun companion(nombre: String): String
    val startLocation: String
    val serviceNotes: String
    val registerCheckin: String
    val enterStartLocation: String
    fun checkinEnabled(hora: String): String
    val registerCheckout: String
    val serviceCompleted: String
    val locationTitle: String
    val mapTitle: String
    val mapPickup: String
    val mapCurrentLocation: String
    val locationGps: String
    val locationRefresh: String
    val locationRequestPermission: String
    val locationPermissionDenied: String
    val locationUnavailable: String
    val locationAccuracy: String
    val locationDistance: String
    val locationInside: String
    val locationOutside: String
    val locationTime: String
    val locationNoGeofence: String
    val locationGetting: String
    val lateArrival: String
    val onTimeArrival: String
    val lastPositionTitle: String
    val lastPositionNone: String
    val reportLocation: String
    val dropoffLocation: String
    val checkoutWithLocation: String
    val geofenceRadius: String
    val outsideZoneAlert: String
    val locationRequired: String
    fun locationAccuracyValue(metros: Int): String
    fun locationDistanceValue(metros: Int): String
    fun locationOutsideValue(metros: Int, radio: Int): String
    fun locationInsideValue(metros: Int, radio: Int): String
    fun locationCoordinates(latitud: String, longitud: String): String
    fun geofenceRadiusValue(metros: Int): String
    fun arrivalLateValue(minutos: Int): String
    fun arrivalOnTimeValue(minutos: Int): String
    fun lastPositionValue(latitud: String, longitud: String, precision: String, hora: String): String

    val detailTitle: String
    val serviceInfo: String
    val date: String
    val time: String
    val pickupPoint: String
    val destination: String
    val pickupHotel: String
    val passengers: String
    val tourDays: String
    val lunch: String
    val included: String
    val notIncluded: String
    val forYourTrip: String
    val itinerary: String
    val assignedTeam: String
    val holder: String
    val companionRole: String
    val traceability: String
    val rejected: String
    val confirmed: String
    fun reason(valor: String): String
    val reassigned: String
    val ratingLabel: String
    val respondAssignment: String
    val observationLabel: String
    val confirmAssignment: String
    val rejectAssignment: String
    val rejectReasonRequired: String
    val startService: String
    val reassignService: String
    val rateService: String
    val traceabilityNote: String
    fun confirmAssignmentText(servicioId: String): String
    val noCandidates: String
    val selectNewHolder: String
    val reassign: String
    val rateDialogTitle: String
    val rateQuestion: String
    fun stars(valor: Int): String
    val coordination: String

    val summaryTitle: String
    val coordinationLocal: String
    val assignmentStatus: String
    val pendingResponse: String
    val confirmedPlural: String
    val rejectedPlural: String
    fun activeSummary(activos: Int, enEjecucion: Int, historial: Int): String
    val viewAllServices: String
    val consultHistory: String
    val resourceAvailability: String
    fun availableNow(disponibles: Int, total: Int): String
    val viewFullTeam: String
    val syncing: String
    val syncNow: String
    fun lastSync(valor: String): String
    val notSynced: String

    val teamTitle: String
    val teamSubtitle: String
    val teamSearch: String
    val teamNoMatches: String
    val teamNoCriteria: String
    val clearSearch: String

    val personTitle: String
    val personNotFound: String
    val contact: String
    val email: String
    val phone: String
    val area: String
    val performance: String
    val noRatings: String
    val accumulatedLabel: String
    val activeServices: String
    val noActiveServicesAssigned: String
    val tapService: String

    val availabilityTitle: String
    val availabilityIntro: String
    val previousMonth: String
    val nextMonth: String
    val weekdays: List<String>
    val selectedDay: String
    val chooseAnotherDate: String
    val blockDay: String
    val noAssignmentsThatDay: String
    val saveAvailability: String
    val changesLocal: String

    val msgAssignmentConfirmed: String
    val msgAssignmentRejected: String
    val msgCheckinRegistered: String
    val msgLocationReported: String
    val msgServiceFinished: String
    val msgRatingSaved: String
    val msgServiceReassigned: String
    val msgProfileUpdated: String
    fun msgSyncCompleted(enviadas: Int): String
    val msgIncorrectCredentials: String
    val msgSyncFailed: String
    val msgSignInAgain: String
    val msgOperationFailed: String
    val msgDataAccessFailed: String
    val msgAgendaReadFailed: String
    val msgAvailabilitySavedLocal: String
    val msgSaveFailed: String

    fun traducir(mensaje: String?): String?
}

object TextosEspanol : Textos {
    override val appName = "Proyecto Lince"
    override val appTagline = "Servicios claros, respuestas a tiempo"
    override val back = "Volver"
    override val logout = "Cerrar sesión"
    override val navHome = "Inicio"
    override val navServices = "Servicios"
    override val navTeam = "Equipo"
    override val navAgenda = "Agenda"
    override val navProfile = "Perfil"
    override val language = "Idioma"
    override val languageSpanish = "Español"
    override val languageEnglish = "English"
    override val confirm = "Confirmar"
    override val cancel = "Cancelar"
    override val save = "Guardar"
    override val accept = "Aceptar"
    override val all = "Todos"
    override val viewAll = "Ver todos"
    override val viewDetails = "Ver detalle"
    override val pending = "Pendiente"
    override val unassigned = "Sin asignar"
    override val id = "ID"

    override fun modoTema(modo: ModoTema): String = when (modo) {
        ModoTema.SISTEMA -> "Sistema"
        ModoTema.CLARO -> "Claro"
        ModoTema.OSCURO -> "Oscuro"
    }

    override fun rol(rol: Rol): String = when (rol) {
        Rol.CONDUCTOR -> "Conductor"
        Rol.GUIA -> "Guía"
        Rol.COORDINADOR -> "Coordinador"
    }

    override fun disponibilidad(valor: Disponibilidad): String = when (valor) {
        Disponibilidad.DISPONIBLE -> "Disponible"
        Disponibilidad.EN_SERVICIO -> "En servicio"
        Disponibilidad.NO_DISPONIBLE -> "No disponible"
    }

    override fun estado(valor: EstadoServicio): String = when (valor) {
        EstadoServicio.PENDIENTE -> "Pendiente"
        EstadoServicio.CONFIRMADA -> "Confirmada"
        EstadoServicio.COMPLETADA -> "Completada"
        EstadoServicio.RECHAZADA -> "Rechazada"
    }

    override fun idioma(valor: Idioma): String = when (valor) {
        Idioma.ESPANOL -> "Español"
        Idioma.INGLES -> "Inglés"
    }

    override fun modalidad(valor: Modalidad): String = when (valor) {
        Modalidad.TRANSFER -> "Transfer"
        Modalidad.MEDIO_DIA -> "Medio día"
        Modalidad.DIA_COMPLETO -> "Día completo"
    }

    override val connectionOffline = "Sin Internet · consultando datos locales"
    override val connectionOnline = "Con Internet · datos locales al día"
    override fun pendingSync(cantidad: Int) = "$cantidad cambio(s) por sincronizar"

    override val loginLogoDescription = "Logo Lince"
    override val loginWelcome = "Bienvenido"
    override val loginSubtitle = "Ingresa con tu cuenta de prueba."
    override val loginUser = "Usuario"
    override val loginPassword = "Contraseña"
    override val loginShowPassword = "Mostrar contraseña"
    override val loginHidePassword = "Ocultar contraseña"
    override val loginButton = "Iniciar sesión"
    override val loginDemoTitle = "Cuentas de prueba · clave Lince123"
    override val loginDemoHint = "Toca una cuenta para completar el ingreso."
    override val loginUse = "Usar"
    override val loginFooter = "El rol se valida al ingresar. Solo se utilizan datos ficticios."

    override val profileTitle = "Mi perfil"
    override val profileId = "ID de usuario (solo lectura)"
    override val profileName = "Nombre"
    override val profileEmail = "Correo"
    override val profilePhone = "Teléfono"
    override val profileZone = "Zona de trabajo"
    override val profileLanguages = "Idiomas que dominas"
    override val profileSave = "Guardar cambios"
    override val profileViewTeam = "Ver equipo"
    override val profileAppearance = "Apariencia"
    override val profileAppearanceHint = "Elige cómo se ve la aplicación en este dispositivo."
    override val profileLanguage = "Idioma de la aplicación"
    override val profileLanguageHint = "Elige el idioma de la interfaz."
    override val profileDataLocal = "Los datos se guardan en este dispositivo y se sincronizan al recuperar la conexión. La cuenta y el rol no se pueden modificar."
    override val errorName = "El nombre debe tener entre 2 y 60 caracteres."
    override val errorEmail = "Ingresa un correo válido."
    override val errorPhone = "Ingresa un teléfono válido."
    override val errorZone = "Indica tu zona de trabajo."
    override val errorLanguages = "Selecciona al menos un idioma."
    override fun accumulated(monto: String) = "Acumulado: $monto"
    override fun averageRating(valor: String) = "Calificación promedio: $valor/5"

    override fun homeGreeting(nombre: String) = "Hola, $nombre"
    override val homeUserFallback = "usuario"
    override val homeDay = "TU JORNADA"
    override fun homePending(cantidad: Int) = "$cantidad servicios por revisar"
    override fun homeTodayAssigned(cantidad: Int) = "Hoy tienes $cantidad servicio(s) asignado(s)."
    override val homeTodayServices = "Servicios de hoy"
    override val homeNextService = "Próximo servicio"
    override val homeNoActive = "Sin servicios activos"
    override val homeNoActiveMessage = "No tienes servicios asignados por ahora."
    override val homeUpdateAgenda = "Actualizar mi agenda"
    override val homeViewServices = "Ver mis servicios"
    override val homeViewHistory = "Consultar historial"
    override val homeViewTeam = "Ver equipo (guías y conductores)"
    override val homeAvailability = "Disponibilidad"
    override val homeAvailabilityHint = "Recuerda bloquear con 7 días de anticipación los días en que no puedas trabajar."

    override val servicesTitle = "Servicios"
    override val servicesAllSubtitle = "Todos los servicios del sistema"
    override val servicesMineSubtitle = "Tus asignaciones"
    override val servicesSearch = "Buscar por fecha, estado o tipo"
    override val servicesAllFilter = "Todas"
    override val servicesPendingFilter = "Pendientes"
    override val servicesConfirmedFilter = "Confirmadas"
    override val servicesNoResults = "Sin resultados"
    override val servicesNoMatch = "No hay servicios que coincidan con el filtro."
    override val servicesClearFilters = "Limpiar filtros"
    override val today = "HOY"
    override val lunchIncluded = "Almuerzo incluido"

    override val historyTitle = "Historial"
    override val historyGeneral = "HISTORIAL GENERAL"
    override val historyAccumulated = "ACUMULADO"
    override fun historyRegistered(cantidad: Int) = "$cantidad servicios registrados"
    override val historyIncludes = "Incluye completados y rechazados."
    override val historyConfirmedCompleted = "Servicios confirmados y completados."
    override val historyCompletedFilter = "Completados"
    override val historyRejectedFilter = "Rechazados"
    override val historyEmpty = "Historial vacío"
    override val historyEmptyMessage = "Todavía no hay servicios en tu historial."
    override fun responsible(nombre: String) = "Responsable: $nombre"
    override fun rejectedAt(valor: String) = "Rechazado: $valor"
    override fun confirmedAt(valor: String) = "Confirmado: $valor"
    override fun rating(value: String) = "Calificación: $value"
    override val fare = "Tarifa"

    override val executionTitle = "Ejecución del servicio"
    override val serviceNotFound = "No se encontró el servicio."
    override val executionMilestones = "Registro de hitos"
    override fun observations(valor: String) = "Observaciones: $valor"
    override val responsibleTitle = "Responsable"
    override val viewHolderProfile = "Ver ficha del titular"
    override fun companion(nombre: String) = "Acompañante: $nombre"
    override val startLocation = "Ubicación de inicio (texto manual)"
    override val serviceNotes = "Observaciones del servicio (opcional)"
    override val registerCheckin = "Registrar check-in"
    override val enterStartLocation = "Ingresa la ubicación de inicio."
    override fun checkinEnabled(hora: String) =
        "El check-in se habilita 15 minutos antes ($hora)."
    override val registerCheckout = "Registrar check-out"
    override val serviceCompleted = "Servicio completado. Ya está disponible en el historial."
    override val locationTitle = "Ubicación y geocerca"
    override val mapTitle = "Mapa"
    override val mapPickup = "Punto de recogida"
    override val mapCurrentLocation = "Tu ubicación"
    override val locationGps = "Ubicación GPS"
    override val locationRefresh = "Actualizar ubicación"
    override val locationRequestPermission = "Permitir ubicación"
    override val locationPermissionDenied =
        "Permiso de ubicación denegado. Actívalo para registrar el check-in."
    override val locationUnavailable = "No se pudo obtener la ubicación. Inténtalo nuevamente."
    override val locationAccuracy = "Precisión GPS"
    override val locationDistance = "Distancia a la geocerca"
    override val locationInside = "Dentro de la geocerca"
    override val locationOutside = "Fuera de la geocerca"
    override val locationTime = "Hora de la ubicación"
    override val locationNoGeofence = "La geocerca del hotel de recogida no está configurada."
    override val locationGetting = "Obteniendo ubicación..."
    override val lateArrival = "Tardanza"
    override val onTimeArrival = "Llegada a tiempo"
    override val lastPositionTitle = "Última posición reportada"
    override val lastPositionNone = "Sin posición reportada."
    override val reportLocation = "Reportar ubicación"
    override val dropoffLocation = "Ubicación de dropoff"
    override val checkoutWithLocation = "Registrar check-out con ubicación"
    override val geofenceRadius = "Radio de geocerca"
    override val outsideZoneAlert = "Fuera de zona"
    override val locationRequired = "Se necesita la ubicación GPS para continuar."
    override fun locationAccuracyValue(metros: Int) = "± $metros m"
    override fun locationDistanceValue(metros: Int) = "$metros m"
    override fun locationOutsideValue(metros: Int, radio: Int) =
        "Fuera de la geocerca: $metros m (radio $radio m)"
    override fun locationInsideValue(metros: Int, radio: Int) =
        "Dentro de la geocerca: $metros m (radio $radio m)"
    override fun locationCoordinates(latitud: String, longitud: String) = "$latitud, $longitud"
    override fun geofenceRadiusValue(metros: Int) = "$metros m"
    override fun arrivalLateValue(minutos: Int) =
        "Tardanza: llegada $minutos min después del pickup."
    override fun arrivalOnTimeValue(minutos: Int) =
        "Llegada $minutos min antes del pickup."
    override fun lastPositionValue(
        latitud: String,
        longitud: String,
        precision: String,
        hora: String
    ) = "Lat $latitud · Lon $longitud · Precisión $precision · $hora"

    override val detailTitle = "Detalle del servicio"
    override val serviceInfo = "Información del servicio"
    override val date = "Fecha"
    override val time = "Hora"
    override val pickupPoint = "Punto de salida"
    override val destination = "Destino"
    override val pickupHotel = "Hotel de recogida"
    override val passengers = "Pasajeros"
    override val tourDays = "Días de tour"
    override val lunch = "Almuerzo"
    override val included = "Incluido"
    override val notIncluded = "No incluido"
    override val forYourTrip = "Para tu recorrido"
    override val itinerary = "Itinerario"
    override val assignedTeam = "Equipo asignado"
    override val holder = "Titular"
    override val companionRole = "Acompañante"
    override val traceability = "Trazabilidad"
    override val rejected = "Rechazada"
    override val confirmed = "Confirmada"
    override fun reason(valor: String) = "Motivo: $valor"
    override val reassigned = "Reasignado"
    override val ratingLabel = "Calificación"
    override val respondAssignment = "Responde la asignación"
    override val observationLabel = "Observación o motivo de rechazo (opcional)"
    override val confirmAssignment = "Confirmar asignación"
    override val rejectAssignment = "Rechazar asignación"
    override val rejectReasonRequired = "Para rechazar, indica el motivo."
    override val startService = "Iniciar servicio (check-in)"
    override val reassignService = "Reasignar servicio"
    override val rateService = "Calificar servicio"
    override val traceabilityNote =
        "Cada respuesta queda registrada con autor y fecha para mantener la trazabilidad."
    override fun confirmAssignmentText(servicioId: String) =
        "Se notificará al coordinador que aceptaste el servicio $servicioId."
    override val noCandidates = "No hay candidatos disponibles con el mismo rol e idioma."
    override val selectNewHolder = "Selecciona al nuevo titular:"
    override val reassign = "Reasignar"
    override val rateDialogTitle = "Calificar servicio"
    override val rateQuestion = "¿Cómo fue el desempeño del titular?"
    override fun stars(valor: Int) = "$valor estrellas"
    override val coordination = "Coordinación"

    override val summaryTitle = "Resumen operativo"
    override val coordinationLocal = "Coordinación · Datos locales"
    override val assignmentStatus = "Estado de las asignaciones"
    override val pendingResponse = "Pendientes de respuesta"
    override val confirmedPlural = "Confirmadas"
    override val rejectedPlural = "Rechazadas"
    override fun activeSummary(activos: Int, enEjecucion: Int, historial: Int) =
        "$activos servicios activos · $enEjecucion en ejecución · $historial en historial"
    override val viewAllServices = "Ver todos los servicios"
    override val consultHistory = "Consultar historial"
    override val resourceAvailability = "Disponibilidad de recursos"
    override fun availableNow(disponibles: Int, total: Int) = "$disponibles de $total disponibles ahora"
    override val viewFullTeam = "Ver equipo completo"
    override val syncing = "Sincronizando..."
    override val syncNow = "Sincronizar ahora"
    override fun lastSync(valor: String) = "Última sincronización: $valor"
    override val notSynced = "sin sincronizar"

    override val teamTitle = "Equipo"
    override val teamSubtitle = "Guías y conductores disponibles"
    override val teamSearch = "Buscar por nombre o rol"
    override val teamNoMatches = "Sin coincidencias"
    override val teamNoCriteria = "No se encontraron personas con ese criterio."
    override val clearSearch = "Limpiar búsqueda"

    override val personTitle = "Ficha del equipo"
    override val personNotFound = "No se encontró la persona."
    override val contact = "Contacto"
    override val email = "Correo"
    override val phone = "Teléfono"
    override val area = "Zona"
    override val performance = "Desempeño"
    override val noRatings = "Sin calificaciones"
    override val accumulatedLabel = "Acumulado"
    override val activeServices = "Servicios activos"
    override val noActiveServicesAssigned = "Sin servicios activos asignados."
    override val tapService = "Toca un servicio para ver el detalle."

    override val availabilityTitle = "Mi disponibilidad"
    override val availabilityIntro =
        "Marca los días en que no podrás trabajar. Los bloqueos requieren 7 días de anticipación."
    override val previousMonth = "Mes anterior"
    override val nextMonth = "Mes siguiente"
    override val weekdays = listOf("L", "M", "X", "J", "V", "S", "D")
    override val selectedDay = "Día seleccionado"
    override val chooseAnotherDate = "Elegir otra fecha"
    override val blockDay = "Bloquear este día"
    override val noAssignmentsThatDay = "No recibirás asignaciones para este día."
    override val saveAvailability = "Guardar disponibilidad"
    override val changesLocal =
        "Los cambios se guardan en este dispositivo y se sincronizan al recuperar la conexión. No puedes bloquear un día con un servicio asignado."

    override val msgAssignmentConfirmed = "Asignación confirmada."
    override val msgAssignmentRejected = "Asignación rechazada."
    override val msgCheckinRegistered = "Check-in registrado."
    override val msgLocationReported = "Ubicación reportada."
    override val msgServiceFinished = "Servicio finalizado."
    override val msgRatingSaved = "Calificación guardada."
    override val msgServiceReassigned = "Servicio reasignado."
    override val msgProfileUpdated = "Perfil actualizado."
    override fun msgSyncCompleted(enviadas: Int) =
        if (enviadas == 0) "Sin cambios pendientes de sincronizar."
        else "Sincronización completada: $enviadas registro(s) enviado(s)."
    override val msgIncorrectCredentials = "Usuario o contraseña incorrectos."
    override val msgSyncFailed = "No se pudo sincronizar. Se reintentará al recuperar la conexión."
    override val msgSignInAgain = "Debes iniciar sesión nuevamente."
    override val msgOperationFailed = "No se pudo completar la operación."
    override val msgDataAccessFailed = "No se pudo acceder a los datos. Intenta nuevamente."
    override val msgAgendaReadFailed = "No se pudo leer tu agenda. Intenta nuevamente."
    override val msgAvailabilitySavedLocal = "Disponibilidad guardada en este dispositivo. Se sincronizará al recuperar la conexión."
    override val msgSaveFailed = "No se pudo guardar. Intenta nuevamente."

    override fun traducir(mensaje: String?): String? = mensaje
}

object TextosIngles : Textos {
    override val appName = "Proyecto Lince"
    override val appTagline = "Clear services, on-time responses"
    override val back = "Back"
    override val logout = "Log out"
    override val navHome = "Home"
    override val navServices = "Services"
    override val navTeam = "Team"
    override val navAgenda = "Schedule"
    override val navProfile = "Profile"
    override val language = "Language"
    override val languageSpanish = "Español"
    override val languageEnglish = "English"
    override val confirm = "Confirm"
    override val cancel = "Cancel"
    override val save = "Save"
    override val accept = "OK"
    override val all = "All"
    override val viewAll = "View all"
    override val viewDetails = "View details"
    override val pending = "Pending"
    override val unassigned = "Unassigned"
    override val id = "ID"

    override fun modoTema(modo: ModoTema): String = when (modo) {
        ModoTema.SISTEMA -> "System"
        ModoTema.CLARO -> "Light"
        ModoTema.OSCURO -> "Dark"
    }

    override fun rol(rol: Rol): String = when (rol) {
        Rol.CONDUCTOR -> "Driver"
        Rol.GUIA -> "Guide"
        Rol.COORDINADOR -> "Coordinator"
    }

    override fun disponibilidad(valor: Disponibilidad): String = when (valor) {
        Disponibilidad.DISPONIBLE -> "Available"
        Disponibilidad.EN_SERVICIO -> "On service"
        Disponibilidad.NO_DISPONIBLE -> "Unavailable"
    }

    override fun estado(valor: EstadoServicio): String = when (valor) {
        EstadoServicio.PENDIENTE -> "Pending"
        EstadoServicio.CONFIRMADA -> "Confirmed"
        EstadoServicio.COMPLETADA -> "Completed"
        EstadoServicio.RECHAZADA -> "Rejected"
    }

    override fun idioma(valor: Idioma): String = when (valor) {
        Idioma.ESPANOL -> "Spanish"
        Idioma.INGLES -> "English"
    }

    override fun modalidad(valor: Modalidad): String = when (valor) {
        Modalidad.TRANSFER -> "Transfer"
        Modalidad.MEDIO_DIA -> "Half day"
        Modalidad.DIA_COMPLETO -> "Full day"
    }

    override val connectionOffline = "No Internet · viewing local data"
    override val connectionOnline = "Online · local data up to date"
    override fun pendingSync(cantidad: Int) = "$cantidad change(s) pending sync"

    override val loginLogoDescription = "Lince logo"
    override val loginWelcome = "Welcome"
    override val loginSubtitle = "Sign in with your test account."
    override val loginUser = "Username"
    override val loginPassword = "Password"
    override val loginShowPassword = "Show password"
    override val loginHidePassword = "Hide password"
    override val loginButton = "Sign in"
    override val loginDemoTitle = "Test accounts · password Lince123"
    override val loginDemoHint = "Tap an account to fill in the credentials."
    override val loginUse = "Use"
    override val loginFooter = "Role is validated on sign-in. Only fictional data is used."

    override val profileTitle = "My profile"
    override val profileId = "User ID (read-only)"
    override val profileName = "Name"
    override val profileEmail = "Email"
    override val profilePhone = "Phone"
    override val profileZone = "Work area"
    override val profileLanguages = "Languages you speak"
    override val profileSave = "Save changes"
    override val profileViewTeam = "View team"
    override val profileAppearance = "Appearance"
    override val profileAppearanceHint = "Choose how the app looks on this device."
    override val profileLanguage = "App language"
    override val profileLanguageHint = "Choose the interface language."
    override val profileDataLocal = "Data is saved on this device and synced when the connection returns. Account and role cannot be changed."
    override val errorName = "The name must be between 2 and 60 characters."
    override val errorEmail = "Enter a valid email."
    override val errorPhone = "Enter a valid phone number."
    override val errorZone = "Enter your work area."
    override val errorLanguages = "Select at least one language."
    override fun accumulated(monto: String) = "Total earned: $monto"
    override fun averageRating(valor: String) = "Average rating: $valor/5"

    override fun homeGreeting(nombre: String) = "Hello, $nombre"
    override val homeUserFallback = "user"
    override val homeDay = "YOUR DAY"
    override fun homePending(cantidad: Int) = "$cantidad services to review"
    override fun homeTodayAssigned(cantidad: Int) = "You have $cantidad service(s) assigned today."
    override val homeTodayServices = "Today's services"
    override val homeNextService = "Next service"
    override val homeNoActive = "No active services"
    override val homeNoActiveMessage = "You have no assigned services right now."
    override val homeUpdateAgenda = "Update my schedule"
    override val homeViewServices = "View my services"
    override val homeViewHistory = "View history"
    override val homeViewTeam = "View team (guides and drivers)"
    override val homeAvailability = "Availability"
    override val homeAvailabilityHint = "Remember to block days off at least 7 days in advance."

    override val servicesTitle = "Services"
    override val servicesAllSubtitle = "All services in the system"
    override val servicesMineSubtitle = "Your assignments"
    override val servicesSearch = "Search by date, status or type"
    override val servicesAllFilter = "All"
    override val servicesPendingFilter = "Pending"
    override val servicesConfirmedFilter = "Confirmed"
    override val servicesNoResults = "No results"
    override val servicesNoMatch = "No services match the filter."
    override val servicesClearFilters = "Clear filters"
    override val today = "TODAY"
    override val lunchIncluded = "Lunch included"

    override val historyTitle = "History"
    override val historyGeneral = "GENERAL HISTORY"
    override val historyAccumulated = "TOTAL EARNED"
    override fun historyRegistered(cantidad: Int) = "$cantidad services recorded"
    override val historyIncludes = "Includes completed and rejected."
    override val historyConfirmedCompleted = "Confirmed and completed services."
    override val historyCompletedFilter = "Completed"
    override val historyRejectedFilter = "Rejected"
    override val historyEmpty = "Empty history"
    override val historyEmptyMessage = "There are no services in your history yet."
    override fun responsible(nombre: String) = "Assigned to: $nombre"
    override fun rejectedAt(valor: String) = "Rejected: $valor"
    override fun confirmedAt(valor: String) = "Confirmed: $valor"
    override fun rating(value: String) = "Rating: $value"
    override val fare = "Fare"

    override val executionTitle = "Service execution"
    override val serviceNotFound = "Service not found."
    override val executionMilestones = "Milestone log"
    override fun observations(valor: String) = "Notes: $valor"
    override val responsibleTitle = "Lead"
    override val viewHolderProfile = "View lead profile"
    override fun companion(nombre: String) = "Companion: $nombre"
    override val startLocation = "Start location (manual text)"
    override val serviceNotes = "Service notes (optional)"
    override val registerCheckin = "Register check-in"
    override val enterStartLocation = "Enter the start location."
    override fun checkinEnabled(hora: String) =
        "Check-in opens 15 minutes before ($hora)."
    override val registerCheckout = "Register check-out"
    override val serviceCompleted = "Service completed. It is now available in history."
    override val locationTitle = "Location and geofence"
    override val mapTitle = "Map"
    override val mapPickup = "Pickup point"
    override val mapCurrentLocation = "Your location"
    override val locationGps = "GPS location"
    override val locationRefresh = "Refresh location"
    override val locationRequestPermission = "Allow location"
    override val locationPermissionDenied =
        "Location permission denied. Enable it to register check-in."
    override val locationUnavailable = "Could not get the location. Please try again."
    override val locationAccuracy = "GPS accuracy"
    override val locationDistance = "Distance to geofence"
    override val locationInside = "Inside the geofence"
    override val locationOutside = "Outside the geofence"
    override val locationTime = "Location time"
    override val locationNoGeofence = "The pickup hotel geofence is not configured."
    override val locationGetting = "Getting location..."
    override val lateArrival = "Late arrival"
    override val onTimeArrival = "On-time arrival"
    override val lastPositionTitle = "Last reported position"
    override val lastPositionNone = "No reported position."
    override val reportLocation = "Report location"
    override val dropoffLocation = "Drop-off location"
    override val checkoutWithLocation = "Register check-out with location"
    override val geofenceRadius = "Geofence radius"
    override val outsideZoneAlert = "Outside zone"
    override val locationRequired = "GPS location is required to continue."
    override fun locationAccuracyValue(metros: Int) = "± $metros m"
    override fun locationDistanceValue(metros: Int) = "$metros m"
    override fun locationOutsideValue(metros: Int, radio: Int) =
        "Outside the geofence: $metros m (radius $radio m)"
    override fun locationInsideValue(metros: Int, radio: Int) =
        "Inside the geofence: $metros m (radius $radio m)"
    override fun locationCoordinates(latitud: String, longitud: String) = "$latitud, $longitud"
    override fun geofenceRadiusValue(metros: Int) = "$metros m"
    override fun arrivalLateValue(minutos: Int) =
        "Late: arrival $minutos min after pickup."
    override fun arrivalOnTimeValue(minutos: Int) =
        "Arrival $minutos min before pickup."
    override fun lastPositionValue(
        latitud: String,
        longitud: String,
        precision: String,
        hora: String
    ) = "Lat $latitud · Lon $longitud · Accuracy $precision · $hora"

    override val detailTitle = "Service details"
    override val serviceInfo = "Service information"
    override val date = "Date"
    override val time = "Time"
    override val pickupPoint = "Pickup point"
    override val destination = "Destination"
    override val pickupHotel = "Pickup hotel"
    override val passengers = "Passengers"
    override val tourDays = "Tour days"
    override val lunch = "Lunch"
    override val included = "Included"
    override val notIncluded = "Not included"
    override val forYourTrip = "About your route"
    override val itinerary = "Itinerary"
    override val assignedTeam = "Assigned team"
    override val holder = "Lead"
    override val companionRole = "Companion"
    override val traceability = "Traceability"
    override val rejected = "Rejected"
    override val confirmed = "Confirmed"
    override fun reason(valor: String) = "Reason: $valor"
    override val reassigned = "Reassigned"
    override val ratingLabel = "Rating"
    override val respondAssignment = "Respond to the assignment"
    override val observationLabel = "Note or rejection reason (optional)"
    override val confirmAssignment = "Confirm assignment"
    override val rejectAssignment = "Reject assignment"
    override val rejectReasonRequired = "To reject, enter a reason."
    override val startService = "Start service (check-in)"
    override val reassignService = "Reassign service"
    override val rateService = "Rate service"
    override val traceabilityNote =
        "Every response is recorded with author and date for traceability."
    override fun confirmAssignmentText(servicioId: String) =
        "The coordinator will be notified that you accepted service $servicioId."
    override val noCandidates = "No candidates available with the same role and language."
    override val selectNewHolder = "Select the new lead:"
    override val reassign = "Reassign"
    override val rateDialogTitle = "Rate service"
    override val rateQuestion = "How was the lead's performance?"
    override fun stars(valor: Int) = "$valor stars"
    override val coordination = "Coordination"

    override val summaryTitle = "Operations summary"
    override val coordinationLocal = "Coordination · Local data"
    override val assignmentStatus = "Assignment status"
    override val pendingResponse = "Awaiting response"
    override val confirmedPlural = "Confirmed"
    override val rejectedPlural = "Rejected"
    override fun activeSummary(activos: Int, enEjecucion: Int, historial: Int) =
        "$activos active services · $enEjecucion in progress · $historial in history"
    override val viewAllServices = "View all services"
    override val consultHistory = "View history"
    override val resourceAvailability = "Resource availability"
    override fun availableNow(disponibles: Int, total: Int) = "$disponibles of $total available now"
    override val viewFullTeam = "View full team"
    override val syncing = "Syncing..."
    override val syncNow = "Sync now"
    override fun lastSync(valor: String) = "Last sync: $valor"
    override val notSynced = "not synced"

    override val teamTitle = "Team"
    override val teamSubtitle = "Available guides and drivers"
    override val teamSearch = "Search by name or role"
    override val teamNoMatches = "No matches"
    override val teamNoCriteria = "No people found with that criterion."
    override val clearSearch = "Clear search"

    override val personTitle = "Team profile"
    override val personNotFound = "Person not found."
    override val contact = "Contact"
    override val email = "Email"
    override val phone = "Phone"
    override val area = "Area"
    override val performance = "Performance"
    override val noRatings = "No ratings"
    override val accumulatedLabel = "Total earned"
    override val activeServices = "Active services"
    override val noActiveServicesAssigned = "No active services assigned."
    override val tapService = "Tap a service to view details."

    override val availabilityTitle = "My availability"
    override val availabilityIntro =
        "Mark the days you cannot work. Blocks require 7 days' notice."
    override val previousMonth = "Previous month"
    override val nextMonth = "Next month"
    override val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
    override val selectedDay = "Selected day"
    override val chooseAnotherDate = "Choose another date"
    override val blockDay = "Block this day"
    override val noAssignmentsThatDay = "You will not receive assignments for this day."
    override val saveAvailability = "Save availability"
    override val changesLocal =
        "Changes are saved on this device and synced when the connection returns. You cannot block a day with an assigned service."

    override val msgAssignmentConfirmed = "Assignment confirmed."
    override val msgAssignmentRejected = "Assignment rejected."
    override val msgCheckinRegistered = "Check-in registered."
    override val msgLocationReported = "Location reported."
    override val msgServiceFinished = "Service completed."
    override val msgRatingSaved = "Rating saved."
    override val msgServiceReassigned = "Service reassigned."
    override val msgProfileUpdated = "Profile updated."
    override fun msgSyncCompleted(enviadas: Int) =
        if (enviadas == 0) "No pending changes to sync."
        else "Sync completed: $enviadas record(s) sent."
    override val msgIncorrectCredentials = "Incorrect username or password."
    override val msgSyncFailed = "Could not sync. It will retry when the connection returns."
    override val msgSignInAgain = "Please sign in again."
    override val msgOperationFailed = "The operation could not be completed."
    override val msgDataAccessFailed = "Could not access the data. Please try again."
    override val msgAgendaReadFailed = "Could not read your schedule. Please try again."
    override val msgAvailabilitySavedLocal = "Availability saved on this device. It will sync when the connection returns."
    override val msgSaveFailed = "Could not save. Please try again."

    override fun traducir(mensaje: String?): String? {
        if (mensaje.isNullOrBlank()) return mensaje
        val texto = mensaje.trim()
        return when {
            texto == "Usuario o contraseña incorrectos." -> msgIncorrectCredentials
            texto == "Resumen disponible solo para coordinadores." -> "Summary is only available to coordinators."
            texto == "Solo el titular puede responder esta asignación." -> "Only the lead can respond to this assignment."
            texto == "La asignación ya fue respondida." -> "The assignment has already been answered."
            texto == "Indica un motivo de entre 1 y 300 caracteres." -> "Enter a reason between 1 and 300 characters."
            texto == "Solo el personal asignado registra la ejecución." -> "Only assigned staff can log execution."
            texto == "Solo el titular registra la ejecución." -> "Only the lead can log execution."
            texto == "El servicio debe estar confirmado." -> "The service must be confirmed."
            texto == "El servicio ya tiene un check-in registrado." -> "The service already has a check-in."
            texto == "Indica la ubicación donde comienza el servicio." -> "Enter the location where the service starts."
            texto == "El check-in se habilita 15 minutos antes de la hora asignada." -> "Check-in opens 15 minutes before the scheduled time."
            texto == "La geocerca del hotel de recogida no está configurada." -> "The pickup hotel geofence is not configured."
            texto == "La ubicación GPS recibida no es válida." -> "The GPS location is not valid."
            texto == "Solo el personal asignado reporta su ubicación." -> "Only assigned staff can report their location."
            texto == "Solo el titular reporta su ubicación." -> "Only the lead can report their location."
            texto == "Solo se reporta la ubicación durante un servicio activo." -> "Location can only be reported during an active service."
            texto.startsWith("Check-in fuera de la geocerca:") ->
                "Check-in outside the geofence:" + texto.removePrefix("Check-in fuera de la geocerca:")
            texto == "Primero debes registrar el inicio." -> "You must register the start first."
            texto == "El servicio ya tiene un check-out registrado." -> "The service already has a check-out."
            texto == "El término no puede ser anterior al inicio." -> "The end time cannot be before the start time."
            texto == "Solo coordinación califica los servicios." -> "Only coordination can rate services."
            texto == "Solo se califican servicios completados." -> "Only completed services can be rated."
            texto == "La calificación debe estar entre 1 y 5." -> "The rating must be between 1 and 5."
            texto == "Solo coordinación reasigna servicios." -> "Only coordination can reassign services."
            texto == "Titular actual no encontrado." -> "Current lead not found."
            texto == "La persona seleccionada no existe." -> "The selected person does not exist."
            texto == "La persona ya está asignada a este servicio." -> "The person is already assigned to this service."
            texto == "El reemplazo debe tener el mismo rol que el titular." -> "The replacement must have the same role as the lead."
            texto == "El nombre debe tener entre 2 y 60 caracteres." -> errorName
            texto == "Ingresa un correo válido." -> errorEmail
            texto == "Ingresa un teléfono válido." -> errorPhone
            texto == "Indica tu zona de trabajo." -> errorZone
            texto == "Selecciona al menos un idioma." -> errorLanguages
            texto == "Debes iniciar sesión." -> "You must sign in."
            texto == "Selecciona hoy o una fecha futura." -> "Select today or a future date."
            texto == "Debes avisar con al menos 7 días de anticipación." -> "You must give at least 7 days' notice."
            texto == "No puedes bloquear un día con un servicio asignado." -> "You cannot block a day with an assigned service."
            texto == "La agenda personal corresponde a conductores y guías." -> "Personal schedules are for drivers and guides."
            texto == "La cuenta no es válida." -> "The account is not valid."
            texto == "Ingresa tu usuario y contraseña." -> "Enter your username and password."
            texto == "No tienes permisos para realizar esta operación." -> "You do not have permission to perform this operation."
            texto == "Debes iniciar sesión nuevamente." -> msgSignInAgain
            texto.startsWith("Persona no encontrada:") ->
                "Person not found:" + texto.removePrefix("Persona no encontrada:")
            texto.startsWith("Servicio no encontrado:") ->
                "Service not found:" + texto.removePrefix("Servicio no encontrado:")
            texto.startsWith("La persona no maneja el idioma requerido (") ->
                "The person does not speak the required language (" +
                    texto.substringAfter("(").replace("Español", "Spanish").replace("Inglés", "English")
            else -> texto
        }
    }
}
