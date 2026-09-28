package br.com.porteirointeligente.util

import com.google.gson.JsonObject
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import java.security.spec.MGF1ParameterSpec

/**
 * Cria o payload público do QR Code sem distribuir uma chave simétrica no aplicativo.
 *
 * O formato atual usa uma chave AES aleatória por QR Code, protegida pela chave pública RSA
 * embutida no app. A chave privada fica somente no backend. QR Codes AES antigos são encaminhados
 * ao backend para compatibilidade durante a migração.
 */
object OfflineCryptoHelper {
    private const val GCM_ALGORITHM = "AES/GCM/NoPadding"
    private const val RSA_ALGORITHM = "RSA/ECB/OAEPPadding"
    private const val GCM_KEY_LENGTH = 32
    private val RSA_OAEP_SPEC = OAEPParameterSpec(
        "SHA-256",
        "MGF1",
        MGF1ParameterSpec.SHA256,
        PSource.PSpecified.DEFAULT
    )

    // Chave pública; não é um segredo. A chave privada correspondente deve ser configurada
    // no backend através de QR_PRIVATE_KEY.
    const val QR_PUBLIC_KEY_DER_BASE64 =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsH+osIhBriqe0J4RqBrLuTASa3omb9iy9KWq/rbjLRaaG3EBxyydmLokEsIy3hS6YFn16lDTRKOcBVeSIqVtw6X3IWlzfHj5xnsE+eQ5lOaY9YGg4A69PBENuG9uUEKaHwPUCij8P0oERURZ90tihm/zbLoUEV75Qc41w/vFzAdWgCfSKv8aTfKCLypdfqFl2wCLtV0g7Vx89s9EI3e0RGKeTyJQSCr2LdCAH7LIjjZcYdtdxInjb18MupNrpcM93AVVVn0r2Sp7IAcDj5qOLz2lkf8zU6OpOZ/xU0Qg9yzXTvaNjz8tNZleAldIAm2253E0WtwLDxd7QvHOTY/4vwIDAQAB"

    fun encryptOwnerData(
        ownerId: Long,
        phone: String,
        name: String,
        isOffline: Boolean,
        offlineMessage: String,
        offlineUntil: Long? = null,
        publicKeyDerBase64: String = QR_PUBLIC_KEY_DER_BASE64
    ): String? {
        return try {
            val json = JsonObject().apply {
                // O ID é autenticado pelo AES-GCM junto com os demais campos.
                // O backend compara este valor com o segmento do QR.
                addProperty("i", ownerId)
                addProperty("p", phone)
                addProperty("n", name)
                addProperty("o", if (isOffline) 1 else 0)
                addProperty("m", offlineMessage)
                offlineUntil?.let { addProperty("u", it) }
            }
            val jsonStr = json.toString()

            require(ownerId > 0L) { "O morador precisa estar salvo antes de gerar o QR Code." }

            val aesKeyBytes = ByteArray(GCM_KEY_LENGTH).also(SecureRandom()::nextBytes)
            val aesKey = SecretKeySpec(aesKeyBytes, "AES")
            val cipher = Cipher.getInstance(GCM_ALGORITHM).apply {
                init(Cipher.ENCRYPT_MODE, aesKey)
            }

            val encryptedBytes = cipher.doFinal(jsonStr.toByteArray(StandardCharsets.UTF_8))
            val publicKey = KeyFactory.getInstance("RSA").generatePublic(
                X509EncodedKeySpec(Base64Url.decode(publicKeyDerBase64))
            )
            val encryptedKey = Cipher.getInstance(RSA_ALGORITHM).apply {
                init(Cipher.ENCRYPT_MODE, publicKey, RSA_OAEP_SPEC)
            }.doFinal(aesKeyBytes)

            // v2.id.chaveAES.iv.ciphertext+tag — cada componente é URL-safe.
            listOf(
                "v2",
                ownerId.toString(),
                Base64Url.encode(encryptedKey),
                Base64Url.encode(cipher.iv),
                Base64Url.encode(encryptedBytes)
            ).joinToString(".")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * O app não guarda a chave privada do QR. QR Codes antigos são encaminhados ao backend;
     * o método permanece para compatibilidade com o fluxo de testes/versões intermediárias.
     */
    fun decryptOwnerData(@Suppress("UNUSED_PARAMETER") encryptedText: String): JSONObject? = null
}
