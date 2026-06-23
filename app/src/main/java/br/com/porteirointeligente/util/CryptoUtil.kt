package br.com.porteirointeligente.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

/**
 * Utilitário de criptografia AES-128 para proteção de privacidade dos dados do morador.
 */
object CryptoUtil {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY = "Porteiro2026Key!" // Chave de 16 bytes
    private const val IV = "PorteiroInitIV26"  // Vetor de inicialização de 16 bytes

    /**
     * Criptografa uma string usando AES-128/CBC e codifica o resultado em Base64 URL Safe.
     */
    fun encrypt(plainText: String): String? {
        return try {
            val keySpec = SecretKeySpec(KEY.toByteArray(StandardCharsets.UTF_8), "AES")
            val ivSpec = IvParameterSpec(IV.toByteArray(StandardCharsets.UTF_8))
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.URL_SAFE or Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodifica de Base64 URL Safe e descriptografa usando AES-128/CBC.
     */
    fun decrypt(encryptedText: String): String? {
        return try {
            val keySpec = SecretKeySpec(KEY.toByteArray(StandardCharsets.UTF_8), "AES")
            val ivSpec = IvParameterSpec(IV.toByteArray(StandardCharsets.UTF_8))
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decodedBytes = Base64.decode(encryptedText, Base64.URL_SAFE or Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
