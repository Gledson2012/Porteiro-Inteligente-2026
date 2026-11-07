package br.com.porteirointeligente.util

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Armazena fotos de perfil cifradas no armazenamento privado do aplicativo.
 *
 * O arquivo em `filesDir/profile_photos` contém IV + ciphertext AES-GCM, com a
 * chave protegida pelo Android Keystore. Para exibir a imagem, [resolvePhotoUri]
 * decifra o conteúdo em um arquivo temporário no cache e devolve a URI.
 *
 * Fotos salvas por versões antigas (JPEG em texto puro) continuam funcionando:
 * o cabeçalho JPEG é detectado e o arquivo original é usado como fallback.
 */
object PhotoSaver {

    private const val PHOTO_DIR = "profile_photos"
    private const val DISPLAY_DIR = "profile_photos_display"
    private const val KEY_ALIAS = "porteiro_inteligente_photo_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_LENGTH_BYTES = 12
    private const val TAG_LENGTH_BITS = 128
    // Cabeçalho próprio dos arquivos cifrados. Sem ele, um IV aleatório que
    // começasse com FF D8 seria confundido com um JPEG legado em texto puro.
    private val MAGIC_HEADER = byteArrayOf('P'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 1)

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return keyStore.getKey(KEY_ALIAS, null) as SecretKey
        }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
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
        }.generateKey()
    }

    fun savePhotoToInternalStorage(context: Context, photoUri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                val dir = File(context.filesDir, PHOTO_DIR)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "${UUID.randomUUID()}.jpg")
                val plainBytes = inputStream.readBytes()
                val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                    init(Cipher.ENCRYPT_MODE, getOrCreateKey())
                }
                val encrypted = MAGIC_HEADER + cipher.iv + cipher.doFinal(plainBytes)
                file.writeBytes(encrypted)
                file.toURI().toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Devolve uma URI legível (em cache) para a foto cifrada informada.
     * Retorna null quando o arquivo não existe ou não pode ser decifrado.
     */
    fun resolvePhotoUri(context: Context, storedPath: String): String? {
        val source = runCatching { File(Uri.parse(storedPath).path ?: return null) }.getOrNull()
            ?: return null
        if (!source.exists()) return null

        return try {
            val bytes = source.readBytes()
            val plainBytes = if (isLegacyJpeg(bytes)) {
                bytes
            } else {
                require(bytes.size > MAGIC_HEADER.size + IV_LENGTH_BYTES) { "Foto inválida" }
                require(bytes.copyOfRange(0, MAGIC_HEADER.size).contentEquals(MAGIC_HEADER)) {
                    "Formato de foto desconhecido"
                }
                val iv = bytes.copyOfRange(MAGIC_HEADER.size, MAGIC_HEADER.size + IV_LENGTH_BYTES)
                val encrypted = bytes.copyOfRange(MAGIC_HEADER.size + IV_LENGTH_BYTES, bytes.size)
                Cipher.getInstance(TRANSFORMATION).run {
                    init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
                    doFinal(encrypted)
                }
            }

            val displayDir = File(context.cacheDir, DISPLAY_DIR).apply { mkdirs() }
            val displayFile = File(displayDir, source.name)
            displayFile.writeBytes(plainBytes)
            displayFile.toURI().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deletePhoto(context: Context, photoPath: String) {
        try {
            val file = File(Uri.parse(photoPath).path ?: return).canonicalFile
            val photoDirectory = File(context.filesDir, PHOTO_DIR).canonicalFile

            // photoUri pode vir de um backup; nunca permita que um valor externo
            // seja usado para apagar outro arquivo privado do aplicativo.
            if (file.parentFile == photoDirectory && file.exists()) {
                file.delete()
            }

            val displayFile = File(
                File(context.cacheDir, DISPLAY_DIR).canonicalFile,
                file.name
            )
            if (displayFile.exists()) displayFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isLegacyJpeg(bytes: ByteArray): Boolean =
        bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
}
