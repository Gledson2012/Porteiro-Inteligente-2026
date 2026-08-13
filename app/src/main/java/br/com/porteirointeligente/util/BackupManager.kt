package br.com.porteirointeligente.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import br.com.porteirointeligente.data.local.LocalDataStore
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.data.local.entity.VisitEntity
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.Visit
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ownerRepository: OwnerRepository,
    private val visitRepository: VisitRepository,
    private val cryptoUtil: CryptoUtil,
    private val localDataStore: LocalDataStore
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    data class BackupData(
        val version: Int = 2,
        val exportDate: String = "",
        val owner: Owner? = null,
        val owners: List<Owner>? = emptyList(),
        val visits: List<Visit>? = emptyList()
    )

    suspend fun generateBackupAndShare(passphrase: String) {
        require(passphrase.length >= MIN_PASSPHRASE_LENGTH) {
            "A senha do backup deve ter no mínimo $MIN_PASSPHRASE_LENGTH caracteres"
        }

        val owners = ownerRepository.observeAllOwners().first()
        val visits = visitRepository.observeAllVisits().first()

        val backup = BackupData(
            exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            owner = owners.firstOrNull(),
            owners = owners,
            visits = visits
        )

        val jsonString = gson.toJson(backup)
        val encryptedBackup = PortableBackupCrypto.encrypt(jsonString, passphrase)
            ?: throw IllegalStateException("Não foi possível criptografar o backup")
        val fileName = "Backup_Porteiro_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.pib"
        val file = File(context.cacheDir, fileName)

        try {
            FileOutputStream(file).use { it.write(encryptedBackup.toByteArray(StandardCharsets.UTF_8)) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_SUBJECT, "Backup Porteiro Inteligente")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Salvar Backup")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            file.delete()
            throw e
        }
    }

    /**
     * Restaura os dados a partir de um arquivo de backup JSON selecionado.
     */
    suspend fun restoreBackup(uri: Uri, passphrase: String? = null): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val decryptedJson = if (jsonString.startsWith(PortableBackupCrypto.PREFIX)) {
                PortableBackupCrypto.decrypt(jsonString, passphrase) ?: return false
            } else {
                // Compatibilidade com backups antigos do mesmo aparelho e JSON legado.
                cryptoUtil.decrypt(jsonString)
                    ?: jsonString.takeIf { it.trimStart().startsWith("{") }
                    ?: return false
            }
            val backup = gson.fromJson(decryptedJson, BackupData::class.java) ?: return false
            val visitsToRestore = backup.visits ?: return false

            // Insere os moradores recuperados (suporta versão 2+ com múltiplos owners)
            val ownersToRestore = if (backup.version >= 2 && !backup.owners.isNullOrEmpty()) {
                backup.owners.orEmpty()
            } else {
                backup.owner?.let { listOf(it) } ?: emptyList()
            }

            // A substituição ocorre em uma única transação; falhas não apagam o banco atual.
            localDataStore.replaceAll(
                owners = ownersToRestore.map(OwnerEntity::fromDomain),
                visits = visitsToRestore.map(VisitEntity::fromDomain)
            )

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val MIN_PASSPHRASE_LENGTH = 8
    }
}

/**
 * Formato portátil de backup: salt e IV aleatórios + AES/GCM derivado de uma senha
 * escolhida pelo usuário. A senha nunca é gravada no dispositivo nem no arquivo.
 */
private object PortableBackupCrypto {
    const val PREFIX = "PI_BACKUP_V2:"
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH = 128

    fun encrypt(plainText: String, passphrase: String): String? {
        return try {
            val salt = KeyDerivation.newSalt()
            val key = SecretKeySpec(
                KeyDerivation.derive(passphrase.toCharArray(), salt),
                "AES"
            )
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val payload = salt + cipher.iv + encrypted
            PREFIX + KeyDerivation.encode(payload)
        } catch (_: Exception) {
            null
        }
    }

    fun decrypt(value: String, passphrase: String?): String? {
        if (passphrase.isNullOrBlank()) return null
        return try {
            val payload = KeyDerivation.decode(value.removePrefix(PREFIX))
            val saltLength = 16
            require(payload.size > saltLength + IV_LENGTH)
            val salt = payload.copyOfRange(0, saltLength)
            val ivStart = saltLength
            val iv = payload.copyOfRange(ivStart, ivStart + IV_LENGTH)
            val encrypted = payload.copyOfRange(ivStart + IV_LENGTH, payload.size)
            val key = SecretKeySpec(
                KeyDerivation.derive(passphrase.toCharArray(), salt),
                "AES"
            )
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))
            String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }
}
