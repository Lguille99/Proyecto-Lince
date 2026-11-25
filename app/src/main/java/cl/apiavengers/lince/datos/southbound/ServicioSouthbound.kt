package cl.apiavengers.lince.datos.southbound

import org.json.JSONObject

data class PaxSouthbound(
    val adultos: Int,
    val ninos: Int,
    val total: Int,
    val texto: String
) {
    companion object {
        fun desdeJson(j: JSONObject) = PaxSouthbound(
            j.getInt("adultos"), j.getInt("ninos"), j.getInt("total"), j.getString("texto")
        )
    }
}

data class ServicioSouthbound(
    val id: Int,
    val codigo: String,
    val srv: String,
    val tipo: String,
    val fecha: String,
    val horaPickup: String,
    val descripcion: String,
    val descripcionPadre: String?,
    val estadoServicio: String,
    val estadoReserva: String,
    val reserva: String,
    val pasajero: String,
    val pax: PaxSouthbound,
    val pickup: String,
    val dropoff: String,
    val notas: String?,
    val oficina: OficinaSouthbound,
    val cliente: ClienteRefSouthbound,
    val idiomaRequerido: IdiomaSouthbound?,
    val guia: PersonaSouthbound?,
    val transportista: PersonaSouthbound?,
    val vehiculo: VehiculoSouthbound?,
    val statusPL: String?,
    val asignacionActualizadoEn: String?
) {
    companion object {
        fun desdeJson(j: JSONObject): ServicioSouthbound {
            val reserva = j.getJSONObject("reserva")
            return ServicioSouthbound(
                id = j.getInt("id"),
                codigo = j.getString("codigo"),
                srv = j.getString("srv"),
                tipo = j.getString("tipo"),
                fecha = j.getString("fecha"),
                horaPickup = j.getString("horaPickup"),
                descripcion = j.getString("descripcion"),
                descripcionPadre = j.textoOpcional("descripcionPadre"),
                estadoServicio = j.getString("estadoServicio"),
                estadoReserva = reserva.getString("estado"),
                reserva = reserva.getString("referencia"),
                pasajero = reserva.getString("nombre"),
                pax = PaxSouthbound.desdeJson(j.getJSONObject("pax")),
                pickup = j.getString("pickup"),
                dropoff = j.getString("dropoff"),
                notas = j.textoOpcional("notas"),
                oficina = OficinaSouthbound.desdeJson(j.getJSONObject("oficina")),
                cliente = ClienteRefSouthbound.desdeJson(j.getJSONObject("cliente")),
                idiomaRequerido = j.objetoOpcional("idiomaRequerido")?.let(IdiomaSouthbound::desdeJson),
                guia = j.objetoOpcional("guia")?.let(PersonaSouthbound::desdeJson),
                transportista = j.objetoOpcional("transportista")?.let(PersonaSouthbound::desdeJson),
                vehiculo = j.objetoOpcional("vehiculo")?.let(VehiculoSouthbound::desdeJson),
                statusPL = j.objetoOpcional("asignacion")?.textoOpcional("statusPL"),
                asignacionActualizadoEn = j.objetoOpcional("asignacion")?.textoOpcional("actualizadoEn")
            )
        }

        fun paginaDesdeJson(texto: String) = paginaDesde(texto, ::desdeJson)
    }
}
