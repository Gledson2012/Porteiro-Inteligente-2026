package br.com.porteirointeligente.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Serviço de Sincronização Firebase (Simulado).
 *
 * Simula o armazenamento dos dados do proprietário no Firebase Realtime Database.
 * Em produção, substituir pelas chamadas reais à SDK do Firebase.
 *
 * ══════════════════════════════════════════════════════
 * EXEMPLO DE ESTRUTURA NO FIREBASE REALTIME DATABASE:
 * ══════════════════════════════════════════════════════
 *
 * {
 *   "owners": {
 *     "-ABC123": {
 *       "id": 1,
 *       "nome": "João Silva",
 *       "endereco": "Rua das Flores, 123, Centro, São Paulo - SP",
 *       "cep": "01310-000",
 *       "apartamento": "101",
 *       "telefone": "5511999999999",
 *       "photoUri": null,
 *       "qrCodePayload": "https://porteiro-inteligente.web.app/scan/1_-1234567890",
 *       "dataCadastro": 1700000000000,
 *       "dataSync": "2024-01-15T10:30:00"
 *     }
 *   },
 *   "visits": {
 *     "-DEF456": {
 *       "id": 1,
 *       "nome": "Carlos Entregador",
 *       "apartamento": "101",
 *       "dataEntrada": 1700000000000,
 *       "dataSaida": null,
 *       "status": "ENTRADA_REGISTRADA"
 *     }
 *   }
 * }
 *
 * ══════════════════════════════════════════════════════
 * INTEGRAÇÃO FUTURA COM FIREBASE SDK:
 * ══════════════════════════════════════════════════════
 *
 * dependencies {
 *     implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
 *     implementation("com.google.firebase:firebase-database-ktx")
 *     implementation("com.google.firebase:firebase-auth-ktx")
 * }
 *
 * ══════════════════════════════════════════════════════
 * EXEMPLO COM SDK REAL:
 * ══════════════════════════════════════════════════════
 *
 * val database = Firebase.database
 * val ref = database.getReference("owners")
 * ref.child(ownerId.toString()).setValue(ownerData)
 *     .addOnSuccessListener { Log.d("FIREBASE", "Dados salvos!") }
 *     .addOnFailureListener { Log.e("FIREBASE", "Erro: ${it.message}") }
 * ══════════════════════════════════════════════════════
 */
object FirebaseSyncService {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    /**
     * Status da sincronização com o Firebase.
     */
    sealed class SyncStatus {
        object IDLE : SyncStatus()
        object SYNCING : SyncStatus()
        data class SUCCESS(val timestamp: String) : SyncStatus()
        data class ERROR(val message: String) : SyncStatus()
    }

    /**
     * Simula o envio dos dados do proprietário para o Firebase.
     *
     * @param nome Nome completo do proprietário
     * @param endereco Endereço completo
     * @param cep CEP
     * @param telefone Telefone celular
     * @param apartamento Número da unidade (opcional)
     */
    suspend fun simularEnvio(
        nome: String,
        endereco: String,
        cep: String,
        telefone: String,
        apartamento: String = ""
    ) {
        _syncStatus.value = SyncStatus.SYNCING

        try {
            // Delay simulando latência de rede (1.5s)
            delay(1500)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val now = Date()

            // === DADOS ENVIADOS PARA O FIREBASE ===
            val dadosFirebase = mapOf(
                "nome" to nome.trim(),
                "endereco" to endereco.trim(),
                "cep" to cep.replace(Regex("[^0-9]"), ""),
                "apartamento" to apartamento.trim(),
                "telefone" to telefone.replace(Regex("[^0-9]"), ""),
                "dataSync" to dateFormat.format(now),
                "appVersion" to "0.1.0",
                "platform" to "android"
            )

            // Log dos dados que seriam enviados ao Firebase (console apenas para debug)
            println("[FIREBASE_SYNC] Dados enviados com sucesso: ${dadosFirebase}")

            _syncStatus.value = SyncStatus.SUCCESS(
                timestamp = dateFormat.format(now)
            )
        } catch (e: Exception) {
            println("[FIREBASE_SYNC] Erro ao sincronizar: ${e.message}")
            _syncStatus.value = SyncStatus.ERROR(e.message ?: "Erro desconhecido")
        }
    }

    /**
     * Reseta o status de sincronização para IDLE.
     * Usado em testes para garantir isolamento.
     */
    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus.IDLE
    }

    /**
     * Exemplo de como seria a estrutura de dados no Firebase Realtime Database.
     * @return JSON example string
     */
    fun getJsonSchemaExample(): String = """
        {
          "owners": {
            "-ABC123": {
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
            "-DEF456": {
              "id": 1,
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
