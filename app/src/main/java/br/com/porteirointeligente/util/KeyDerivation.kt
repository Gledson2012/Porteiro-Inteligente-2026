package br.com.porteirointeligente.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Funções comuns para derivar chaves sem persistir senhas em texto puro. */
object KeyDerivation {
    private const val ITERATIONS = 150_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    fun newSalt(): ByteArray = ByteArray(SALT_LENGTH_BYTES).also(SecureRandom()::nextBytes)

    fun derive(password: CharArray, salt: ByteArray): ByteArray {
        val factory = try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        } catch (_: Exception) {
            // Compatibilidade com provedores criptográficos antigos do Android.
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        }
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun encode(bytes: ByteArray): String = Base64Url.encode(bytes)

    fun decode(value: String): ByteArray = Base64Url.decode(value)

    fun securelyEquals(left: ByteArray, right: ByteArray): Boolean =
        MessageDigest.isEqual(left, right)
}
