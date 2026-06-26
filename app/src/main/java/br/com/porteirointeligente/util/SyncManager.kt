package br.com.porteirointeligente.util

import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de sincronização com o backend REST.
 *
 * Os repositórios agora usam flows reativos que sincronizam com a API
 * na primeira coleta e então observam o banco Room. Esta classe coordena
 * a sincronização forçada e o upload de dados locais.
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val ownerRepository: OwnerRepository,
    private val visitRepository: VisitRepository
) {

    /**
     * Obtém a URL do servidor configurada nas preferências.
     */
    fun getServerUrl(): String {
        val prefs = context.getSharedPreferences("sync_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("server_url", "http://10.0.2.2:3000/") ?: "http://10.0.2.2:3000/"
    }

    /**
     * Define a URL do servidor.
     */
    fun setServerUrl(url: String) {
        context.getSharedPreferences("sync_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("server_url", if (url.endsWith("/")) url else "$url/")
            .apply()
    }

    /**
     * Sincroniza dados do backend para o dispositivo (download).
     * Força o refresh dos repositórios ao coletar os flows reativos,
     * que primeiro buscam da API e depois observam o Room.
     */
    suspend fun syncDown(): Boolean {
        return try {
            // Coleta os flows para forçar a sincronização com a API
            ownerRepository.observeAllOwners().first()
            visitRepository.observeAllVisits().first()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Sincroniza dados do dispositivo para o backend (upload).
     */
    suspend fun syncUp(): Boolean {
        return try {
            // Envia moradores locais para o servidor
            val localOwners = ownerRepository.observeAllOwners().first()
            localOwners.forEach { owner ->
                try {
                    val result = if (owner.id > 0L) {
                        // Atualiza via API (que também atualiza o Room local)
                        ownerRepository.updateOwner(owner)
                    } else {
                        // Cria novo via API (que também insere no Room local)
                        ownerRepository.insertOwner(owner)
                    }
                    if (result.isFailure) {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Envia visitas locais para o servidor
            val localVisits = visitRepository.observeAllVisits().first()
            localVisits.forEach { visit ->
                try {
                    val result = if (visit.id > 0L) {
                        // Tenta atualizar a visita no servidor
                        visitRepository.updateVisit(visit)
                    } else {
                        // Cria nova visita no servidor
                        visitRepository.insertVisit(visit)
                    }
                    if (result.isFailure) {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Sincronização bidirecional completa.
     */
    suspend fun syncAll(): Boolean {
        val up = syncUp()
        val down = syncDown()
        return up && down
    }

    companion object {
        /** Verifica se o backend está configurado */
        fun isConfigured(context: android.content.Context): Boolean {
            val url = context.getSharedPreferences("sync_prefs", android.content.Context.MODE_PRIVATE)
                .getString("server_url", null)
            return !url.isNullOrBlank()
        }
    }
}
