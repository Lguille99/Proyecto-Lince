package cl.apiavengers.lince.backend.seguridad

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Password {
    private const val ITERACIONES = 120_000
    private const val BITS = 256

    fun hash(clave: String): String {
        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val derivada = derivar(clave, sal, ITERACIONES)
        return "$ITERACIONES:${Base64.getEncoder().encodeToString(sal)}:" +
            Base64.getEncoder().encodeToString(derivada)
    }

    fun verificar(clave: String, almacenado: String): Boolean {
        val partes = almacenado.split(":")
        if (partes.size != 3) return false
        val iteraciones = partes[0].toIntOrNull() ?: return false
        val sal = runCatching { Base64.getDecoder().decode(partes[1]) }.getOrNull() ?: return false
        val esperado = runCatching { Base64.getDecoder().decode(partes[2]) }.getOrNull() ?: return false
        return MessageDigest.isEqual(derivar(clave, sal, iteraciones), esperado)
    }

    private fun derivar(clave: String, sal: ByteArray, iteraciones: Int): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(clave.toCharArray(), sal, iteraciones, BITS))
            .encoded
}
