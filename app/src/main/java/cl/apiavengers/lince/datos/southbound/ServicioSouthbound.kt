package cl.apiavengers.lince.datos.southbound

import org.json.JSONObject

data class ServicioSouthbound(
    val id: Int,
    val codigo: String,
    val descripcion: String,
    val fecha: String,
    val hora: String,
    val tipo: String,
    val estadoServicio: String,
    val estadoAsignacion: String?,
    val estadoReserva: String,
    val reserva: String,
    val pasajero: String,
    val pasajeros: Int,
    val recogida: String,
    val destino: String,
    val cliente: String,
    val oficina: String,
    val guia: String?,
    val transportista: String?,
    val vehiculo: String?,
    val idioma: String?,
    val notas: String?
) {
    companion object {
        fun desdeJson(j: JSONObject): ServicioSouthbound {
            val reserva = j.getJSONObject("reserva")
            return ServicioSouthbound(
                j.getInt("id"), j.getString("codigo"), j.getString("descripcion"),
                j.getString("fecha"), j.getString("horaPickup"), j.getString("tipo"),
                j.getString("estadoServicio"), j.getJSONObject("asignacion").textoOpcional("statusPL"),
                reserva.getString("estado"), reserva.getString("referencia"), reserva.getString("nombre"),
                j.getJSONObject("pax").getInt("total"), j.getString("pickup"), j.getString("dropoff"),
                j.getJSONObject("cliente").getString("nombre"), j.getJSONObject("oficina").getString("nombre"),
                j.optJSONObject("guia")?.textoOpcional("nombre"),
                j.optJSONObject("transportista")?.textoOpcional("nombre"),
                j.optJSONObject("vehiculo")?.textoOpcional("patente"),
                j.optJSONObject("idiomaRequerido")?.textoOpcional("nombre"), j.textoOpcional("notas")
            )
        }
    }
}

private fun JSONObject.textoOpcional(nombre: String): String? =
    if (isNull(nombre)) null else getString(nombre)

data class PaginaSouthbound(
    val servicios: List<ServicioSouthbound>,
    val pagina: Int,
    val paginas: Int,
    val total: Int
) {
    companion object {
        fun desdeJson(texto: String): PaginaSouthbound {
            val j = JSONObject(texto)
            val datos = j.getJSONArray("data")
            val meta = j.getJSONObject("meta")
            return PaginaSouthbound(
                List(datos.length()) { ServicioSouthbound.desdeJson(datos.getJSONObject(it)) },
                meta.getInt("page"), meta.getInt("totalPages"), meta.getInt("total")
            )
        }
    }
}
