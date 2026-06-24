package br.com.porteirointeligente.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import java.nio.charset.StandardCharsets

/**
 * Utilitário de criptografia AES/GCM com chave armazenada no Android Keystore.
 *
 * A chave criptográfica permanece no hardware seguro do dispositivo (TEE/StrongBox)
 * e nunca é exposta no código-fonte ou memória do app.
 */
@Singleton
class CryptoUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEY_ALIAS = "porteiro_inteligente_qr_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    /**
     * Obtém ou cria a chave AES-256 no Android Keystore.
     * A chave é gerada uma única vez e reutilizada nas operações subsequentes.
     */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        if (keyStore.containsAlias(KEY_ALIAS)) {
            return keyStore.getKey(KEY_ALIAS, null) as SecretKey
        }

        val keyGenerator = javax.crypto.KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        ).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }

        return keyGenerator.generateKey()
    }

    /**
     * Criptografa uma string usando AES/GCM/NoPadding com chave do Android Keystore.
     *
     * Retorna o IV (12 bytes) concatenado ao texto cifrado, codificado em Base64 URL Safe.
     * O IV é único a cada chamada, garantindo segurança mesmo para textos iguais.
     *
     * @param plainText Texto original a ser criptografado
     * @return Base64(IV + ciphertext) ou null em caso de erro
     */
    fun encrypt(plainText: String): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

            // Concatena IV + ciphertext: necessário para descriptografia
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            Base64.encodeToString(combined, Base64.URL_SAFE or Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Descriptografa uma string previamente criptografada por [encrypt].
     *
     * @param encryptedText Base64(IV + ciphertext) gerado por [encrypt]
     * @return Texto original ou null em caso de erro
     */
    fun decrypt(encryptedText: String): String? {
        return try {
            val combined = Base64.decode(encryptedText, Base64.URL_SAFE or Base64.NO_WRAP)

            // Extrai IV (primeiros 12 bytes) e ciphertext (restante)
            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

            val decryptedBytes = cipher.doFinal(ciphertext)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Remove a chave do Android Keystore e gera uma nova.
     * Útil para redefinição de segurança.
     */
    fun resetKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            keyStore.deleteEntry(KEY_ALIAS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
