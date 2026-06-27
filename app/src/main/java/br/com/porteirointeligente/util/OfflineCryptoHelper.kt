package br.com.porteirointeligente.util

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utilitário de criptografia AES-256-CBC para compartilhamento seguro offline de dados do QR Code.
 * Permite que o servidor web Vercel decodifique o telefone e redirecione diretamente para o WhatsApp,
 * sem necessitar de um banco de dados online sincronizado.
 */
object OfflineCryptoHelper {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY = "PorteiroInteligente2026KeySecure" // 32 bytes (256-bit key)
    private const val IV = "1234567890123456" // 16 bytes (128-bit IV)

    fun encryptOwnerData(phone: String, name: String, isOffline: Boolean, offlineMessage: String): String? {
        return try {
            val json = JSONObject().apply {
                put("p", phone)
                put("n", name)
                put("o", if (isOffline) 1 else 0)
                put("m", offlineMessage)
            }
            val jsonStr = json.toString()

            val keySpec = SecretKeySpec(KEY.toByteArray(StandardCharsets.UTF_8), "AES")
            val ivSpec = IvParameterSpec(IV.toByteArray(StandardCharsets.UTF_8))

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

            val encryptedBytes = cipher.doFinal(jsonStr.toByteArray(StandardCharsets.UTF_8))
            // Retorna Base64 URL-safe sem quebras de linha e sem padding para manter a URL limpa
            Base64.encodeToString(encryptedBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decryptOwnerData(encryptedText: String): JSONObject? {
        return try {
            val keySpec = SecretKeySpec(KEY.toByteArray(StandardCharsets.UTF_8), "AES")
            val ivSpec = IvParameterSpec(IV.toByteArray(StandardCharsets.UTF_8))

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            // Converte Base64 URL-safe de volta para os bytes correspondentes
            val decodedBytes = Base64.decode(encryptedText, Base64.URL_SAFE or Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            val jsonStr = String(decryptedBytes, StandardCharsets.UTF_8)
            JSONObject(jsonStr)
        } catch (e: Exception) {
            null
        }
    }
}
