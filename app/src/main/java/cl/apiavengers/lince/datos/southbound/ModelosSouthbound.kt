package cl.apiavengers.lince.datos.southbound

import org.json.JSONObject

internal fun JSONObject.textoOpcional(nombre: String): String? =
    if (!has(nombre) || isNull(nombre)) null else optString(nombre, null)

internal fun JSONObject.enteroOpcional(nombre: String): Int? =
    if (!has(nombre) || isNull(nombre)) null else getInt(nombre)

internal fun JSONObject.booleanoOpcional(nombre: String): Boolean? =
    if (!has(nombre) || isNull(nombre)) null else getBoolean(nombre)

internal fun JSONObject.objetoOpcional(nombre: String): JSONObject? =
    if (!has(nombre) || isNull(nombre)) null else getJSONObject(nombre)

internal fun <T> listaJson(arreglo: org.json.JSONArray, convertir: (JSONObject) -> T): List<T> =
    List(arreglo.length()) { convertir(arreglo.getJSONObject(it)) }

internal fun <T> paginaDesde(texto: String, convertir: (JSONObject) -> T): PaginaSouthbound<T> {
    val j = JSONObject(texto)
    val datos = j.getJSONArray("data")
    return PaginaSouthbound(
        listaJson(datos, convertir),
        MetaSouthbound.desdeJson(j.getJSONObject("meta"))
    )
}

internal fun objetoData(texto: String): JSONObject = JSONObject(texto).getJSONObject("data")

data class MetaSouthbound(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int
) {
    companion object {
        fun desdeJson(j: JSONObject) = MetaSouthbound(
            j.getInt("page"), j.getInt("pageSize"), j.getInt("total"), j.getInt("totalPages")
        )
    }
}

data class PaginaSouthbound<T>(val datos: List<T>, val meta: MetaSouthbound) {
    val pagina: Int get() = meta.page
    val paginas: Int get() = meta.totalPages
    val total: Int get() = meta.total
}

data class OficinaSouthbound(val codigo: String, val nombre: String) {
    companion object {
        fun desdeJson(j: JSONObject) = OficinaSouthbound(j.getString("codigo"), j.getString("nombre"))
    }
}

data class IdiomaSouthbound(val codigo: String, val nombre: String) {
    companion object {
        fun desdeJson(j: JSONObject) = IdiomaSouthbound(j.getString("codigo"), j.getString("nombre"))
    }
}

data class VehiculoSouthbound(
    val id: Int,
    val patente: String,
    val tipo: String,
    val capacidad: Int
) {
    companion object {
        fun desdeJson(j: JSONObject) = VehiculoSouthbound(
            j.getInt("id"), j.getString("patente"), j.getString("tipo"), j.getInt("capacidad")
        )
    }
}

data class PersonaSouthbound(val id: Int, val codigo: String, val nombre: String) {
    companion object {
        fun desdeJson(j: JSONObject) = PersonaSouthbound(
            j.getInt("id"), j.getString("codigo"), j.getString("nombre")
        )
    }
}

data class ClienteRefSouthbound(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val pais: String
) {
    companion object {
        fun desdeJson(j: JSONObject) = ClienteRefSouthbound(
            j.getInt("id"), j.getString("codigo"), j.getString("nombre"), j.getString("pais")
        )
    }
}

data class CodigoDescripcionSouthbound(val codigo: String, val descripcion: String) {
    companion object {
        fun desdeJson(j: JSONObject) = CodigoDescripcionSouthbound(
            j.getString("codigo"), j.getString("descripcion")
        )
    }
}

data class GuiaSouthbound(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val oficina: OficinaSouthbound,
    val idiomas: List<IdiomaSouthbound>,
    val evaluacionPromedio: Double,
    val fechaIngreso: String,
    val activo: Boolean,
    val totalServicios: Int?
) {
    companion object {
        fun desdeJson(j: JSONObject) = GuiaSouthbound(
            id = j.getInt("id"),
            codigo = j.getString("codigo"),
            nombre = j.getString("nombre"),
            oficina = OficinaSouthbound.desdeJson(j.getJSONObject("oficina")),
            idiomas = listaJson(j.getJSONArray("idiomas"), IdiomaSouthbound::desdeJson),
            evaluacionPromedio = j.getDouble("evaluacionPromedio"),
            fechaIngreso = j.getString("fechaIngreso"),
            activo = j.getBoolean("activo"),
            totalServicios = j.enteroOpcional("totalServicios")
        )

        fun paginaDesdeJson(texto: String) = paginaDesde(texto, ::desdeJson)
    }
}

data class TransportistaSouthbound(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val oficina: OficinaSouthbound,
    val vehiculos: List<VehiculoSouthbound>,
    val evaluacionPromedio: Double,
    val fechaIngreso: String,
    val activo: Boolean,
    val totalServicios: Int?
) {
    companion object {
        fun desdeJson(j: JSONObject) = TransportistaSouthbound(
            id = j.getInt("id"),
            codigo = j.getString("codigo"),
            nombre = j.getString("nombre"),
            oficina = OficinaSouthbound.desdeJson(j.getJSONObject("oficina")),
            vehiculos = listaJson(j.getJSONArray("vehiculos"), VehiculoSouthbound::desdeJson),
            evaluacionPromedio = j.getDouble("evaluacionPromedio"),
            fechaIngreso = j.getString("fechaIngreso"),
            activo = j.getBoolean("activo"),
            totalServicios = j.enteroOpcional("totalServicios")
        )

        fun paginaDesdeJson(texto: String) = paginaDesde(texto, ::desdeJson)
    }
}

data class ClienteApiSouthbound(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val pais: String,
    val totalServicios: Int?,
    val totalPax: Int?
) {
    companion object {
        fun desdeJson(j: JSONObject) = ClienteApiSouthbound(
            id = j.getInt("id"),
            codigo = j.getString("codigo"),
            nombre = j.getString("nombre"),
            pais = j.getString("pais"),
            totalServicios = j.enteroOpcional("totalServicios"),
            totalPax = j.enteroOpcional("totalPax")
        )

        fun paginaDesdeJson(texto: String) = paginaDesde(texto, ::desdeJson)
    }
}

data class TipoServicioSouthbound(val tipo: String, val srv: String) {
    companion object {
        fun desdeJson(j: JSONObject) = TipoServicioSouthbound(j.getString("tipo"), j.getString("srv"))
    }
}

data class TipoVehiculoSouthbound(val tipo: String, val capacidad: Int) {
    companion object {
        fun desdeJson(j: JSONObject) = TipoVehiculoSouthbound(j.getString("tipo"), j.getInt("capacidad"))
    }
}

data class CatalogoSouthbound(
    val oficinas: List<OficinaSouthbound>,
    val idiomas: List<IdiomaSouthbound>,
    val tiposServicio: List<TipoServicioSouthbound>,
    val estadosServicio: List<CodigoDescripcionSouthbound>,
    val statusPL: List<CodigoDescripcionSouthbound>,
    val tiposVehiculo: List<TipoVehiculoSouthbound>
) {
    companion object {
        fun desdeJson(j: JSONObject) = CatalogoSouthbound(
            oficinas = listaJson(j.getJSONArray("oficinas"), OficinaSouthbound::desdeJson),
            idiomas = listaJson(j.getJSONArray("idiomas"), IdiomaSouthbound::desdeJson),
            tiposServicio = listaJson(j.getJSONArray("tiposServicio"), TipoServicioSouthbound::desdeJson),
            estadosServicio = listaJson(j.getJSONArray("estadosServicio"), CodigoDescripcionSouthbound::desdeJson),
            statusPL = listaJson(j.getJSONArray("statusPL"), CodigoDescripcionSouthbound::desdeJson),
            tiposVehiculo = listaJson(j.getJSONArray("tiposVehiculo"), TipoVehiculoSouthbound::desdeJson)
        )
    }
}

data class ConteoSouthbound(val clave: String, val servicios: Int, val pax: Int) {
    companion object {
        fun desdeJson(j: JSONObject) = ConteoSouthbound(
            j.getString("clave"), j.getInt("servicios"), j.getInt("pax")
        )
    }
}

data class ResumenSouthbound(
    val fechaDesde: String?,
    val fechaHasta: String?,
    val totalServicios: Int,
    val totalPax: Int,
    val porEstadoServicio: List<ConteoSouthbound>,
    val porStatusPL: List<ConteoSouthbound>,
    val porOficina: List<ConteoSouthbound>,
    val porTipo: List<ConteoSouthbound>,
    val porDia: List<ConteoSouthbound>
) {
    companion object {
        fun desdeJson(j: JSONObject) = ResumenSouthbound(
            fechaDesde = j.textoOpcional("fechaDesde"),
            fechaHasta = j.textoOpcional("fechaHasta"),
            totalServicios = j.getInt("totalServicios"),
            totalPax = j.getInt("totalPax"),
            porEstadoServicio = listaJson(j.getJSONArray("porEstadoServicio"), ConteoSouthbound::desdeJson),
            porStatusPL = listaJson(j.getJSONArray("porStatusPL"), ConteoSouthbound::desdeJson),
            porOficina = listaJson(j.getJSONArray("porOficina"), ConteoSouthbound::desdeJson),
            porTipo = listaJson(j.getJSONArray("porTipo"), ConteoSouthbound::desdeJson),
            porDia = listaJson(j.getJSONArray("porDia"), ConteoSouthbound::desdeJson)
        )
    }
}

data class SaludSouthbound(val status: String, val db: String, val version: String, val time: String) {
    companion object {
        fun desdeJson(j: JSONObject) = SaludSouthbound(
            j.getString("status"), j.getString("db"), j.getString("version"), j.getString("time")
        )
    }
}
