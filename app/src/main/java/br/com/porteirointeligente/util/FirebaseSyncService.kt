package br.com.porteirointeligente.util

import android.content.Context
import android.util.Log
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.Visit
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Status da sincronização com o Firebase Realtime Database.
 */
sealed class SyncStatus {
    object IDLE : SyncStatus()
    object SYNCING : SyncStatus()
    data class SUCCESS(val timestamp: String) : SyncStatus()
    data class ERROR(val message: String) : SyncStatus()
}

/**
 * Serviço de Sincronização Firebase Realtime Database.
 *
 * Sincroniza os dados do aplicativo (moradores e visitas) com o Firebase Realtime Database.
 * Requer o arquivo google-services.json na pasta app/ para funcionar.
 * Se o Firebase não estiver configurado, as operações retornam erro gracefulmente.
 */
@Singleton
class FirebaseSyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ownerRepository: OwnerRepository,
    private val visitRepository: VisitRepository
) {
    companion object {
        private const val TAG = "FIREBASE_SYNC"
        private const val OWNERS_NODE = "owners"
        private const val VISITS_NODE = "visits"
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    private var database: FirebaseDatabase? = null

    private fun getDatabase(): FirebaseDatabase? {
        if (database == null) {
            try {
                database = Firebase.database
                database?.setPersistenceEnabled(true)
            } catch (e: Throwable) {
                Log.w(TAG, "Firebase não disponível (google-services.json ausente ou inválido?)", e)
                return null
            }
        }
        return database
    }

    /**
     * Sincroniza todos os moradores e visitas para o Firebase Realtime Database.
     */
    suspend fun syncAll(): Boolean {
        val db = getDatabase() ?: return false

        _syncStatus.value = SyncStatus.SYNCING

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val now = dateFormat.format(Date())

            // Busca dados locais
            val localOwners = ownerRepository.observeAllOwners().first()
            val localVisits = visitRepository.observeAllVisits().first()

            // Envia moradores para o Firebase
            val ownersRef = db.getReference(OWNERS_NODE)
            localOwners.forEach { owner ->
                val ownerData = owner.toFirebaseMap(now)
                ownersRef.child(owner.id.toString()).setValue(ownerData).await()
            }

            // Envia visitas para o Firebase
            val visitsRef = db.getReference(VISITS_NODE)
            localVisits.forEach { visit ->
                val visitData = visit.toFirebaseMap(now)
                visitsRef.child(visit.id.toString()).setValue(visitData).await()
            }

            Log.i(TAG, "Sincronização concluída: ${localOwners.size} moradores, ${localVisits.size} visitas")
            _syncStatus.value = SyncStatus.SUCCESS(timestamp = now)
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao sincronizar com Firebase", e)
            _syncStatus.value = SyncStatus.ERROR(e.message ?: "Erro desconhecido")
            return false
        }
    }

    /**
     * Sincroniza apenas moradores para o Firebase.
     */
    suspend fun syncOwners(): Boolean {
        val db = getDatabase() ?: return false
        _syncStatus.value = SyncStatus.SYNCING

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val now = dateFormat.format(Date())
            val localOwners = ownerRepository.observeAllOwners().first()
            val ownersRef = db.getReference(OWNERS_NODE)

            localOwners.forEach { owner ->
                ownersRef.child(owner.id.toString()).setValue(owner.toFirebaseMap(now)).await()
            }

            _syncStatus.value = SyncStatus.SUCCESS(timestamp = now)
            true
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR(e.message ?: "Erro desconhecido")
            false
        }
    }

    /**
     * Sincroniza apenas visitas para o Firebase.
     */
    suspend fun syncVisits(): Boolean {
        val db = getDatabase() ?: return false
        _syncStatus.value = SyncStatus.SYNCING

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val now = dateFormat.format(Date())
            val localVisits = visitRepository.observeAllVisits().first()
            val visitsRef = db.getReference(VISITS_NODE)

            localVisits.forEach { visit ->
                visitsRef.child(visit.id.toString()).setValue(visit.toFirebaseMap(now)).await()
            }

            _syncStatus.value = SyncStatus.SUCCESS(timestamp = now)
            true
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR(e.message ?: "Erro desconhecido")
            false
        }
    }

    /**
     * Reseta o status de sincronização para IDLE.
     */
    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus.IDLE
    }

    /**
     * Verifica se o Firebase está configurado no projeto.
     */
    fun isConfigured(): Boolean {
        return try {
            getDatabase() != null
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtém um exemplo da estrutura JSON usada no Firebase.
     */
    fun getJsonSchemaExample(): String = """
        {
          "owners": {
            "1": {
              "id": 1,
              "nome": "João Silva",
              "nomeCondominio": "Residencial Parque Verde",
              "endereco": "Rua das Flores, 123, Centro, São Paulo - SP",
              "cep": "01310123",
              "apartamento": "101",
              "telefone": "5511999999999",
              "photoUri": null,
              "qrCodePayload": "https://porteiro-inteligente.web.app/scan/1_-1234567890",
              "dataCadastro": 1700000000000,
              "isOffline": false,
              "offlineMessage": "",
              "offlineUntil": null,
              "dataSync": "2024-01-15T10:30:00"
            }
          },
          "visits": {
            "1": {
              "id": 1,
              "ownerId": 0,
              "nome": "Carlos Entregador",
              "documento": "123.456.789-00",
              "apartamento": "101",
              "telefone": "5511988888888",
              "motivo": "Entrega de encomenda",
              "dataEntrada": 1700000000000,
              "dataSaida": null,
              "status": "ENTRADA_REGISTRADA"
            }
          }
        }
    """.trimIndent()
}

/** Extensões para converter modelos em mapas para o Firebase */
private fun Owner.toFirebaseMap(dataSync: String): Map<String, Any?> = mapOf(
    "id" to id,
    "nome" to nome.trim(),
    "nomeCondominio" to nomeCondominio.trim(),
    "endereco" to endereco.trim(),
    "cep" to cep.replace(Regex("[^0-9]"), ""),
    "apartamento" to apartamento.trim(),
    "telefone" to telefone.replace(Regex("[^0-9]"), ""),
    "photoUri" to photoUri,
    "qrCodePayload" to qrCodePayload,
    "dataCadastro" to dataCadastro,
    "isOffline" to isOffline,
    "offlineMessage" to offlineMessage,
    "offlineUntil" to offlineUntil,
    "dataSync" to dataSync
)

private fun Visit.toFirebaseMap(dataSync: String): Map<String, Any?> = mapOf(
    "id" to id,
    "ownerId" to 0,
    "nome" to nome.trim(),
    "documento" to documento.trim(),
    "apartamento" to apartamento.trim(),
    "telefone" to telefone.replace(Regex("[^0-9]"), ""),
    "motivo" to motivo.trim(),
    "dataEntrada" to dataEntrada,
    "dataSaida" to dataSaida,
    "status" to status.name,
    "dataSync" to dataSync
)
