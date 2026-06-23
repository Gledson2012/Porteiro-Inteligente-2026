package br.com.porteirointeligente.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.Visit
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ownerRepository: OwnerRepository,
    private val visitRepository: VisitRepository
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    data class BackupData(
        val version: Int = 1,
        val exportDate: String,
        val owner: Owner?,
        val visits: List<Visit>
    )

    suspend fun generateBackupAndShare() {
        val owner = ownerRepository.observeAllOwners().first().firstOrNull()
        val visits = visitRepository.observeAllVisits().first()

        val backup = BackupData(
            exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            owner = owner,
            visits = visits
        )

        val jsonString = gson.toJson(backup)
        val fileName = "Backup_Porteiro_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.json"
        
        try {
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { it.write(jsonString.toByteArray()) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "Backup Porteiro Inteligente")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Salvar Backup")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Restaura os dados a partir de um arquivo de backup JSON selecionado.
     */
    suspend fun restoreBackup(uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val backup = gson.fromJson(jsonString, BackupData::class.java) ?: return false

            // Limpa o banco de dados antes da restauração
            ownerRepository.deleteAll()
            visitRepository.clearAll()

            // Insere o morador recuperado
            backup.owner?.let {
                ownerRepository.insertOwner(it)
            }

            // Insere o histórico de visitas recuperado
            backup.visits.forEach { visit ->
                visitRepository.insertVisit(visit)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
