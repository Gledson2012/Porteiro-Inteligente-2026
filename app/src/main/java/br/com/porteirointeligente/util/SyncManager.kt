package br.com.porteirointeligente.util

import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DTOs para comunicação com a API REST
 */
data class OwnerDto(
    val id: Long = 0,
    val nome: String,
    val nomeCondominio: String = "",
    val endereco: String,
    val cep: String = "",
    val apartamento: String,
    val telefone: String,
    val photoUri: String? = null,
    val qrCodePayload: String,
    val dataCadastro: Long,
    val isOffline: Boolean = false,
    val offlineMessage: String = "",
    val offlineUntil: Long? = null
)

data class VisitDto(
    val id: Long = 0,
    val nome: String,
    val documento: String,
    val apartamento: String,
    val telefone: String,
    val motivo: String,
    val dataEntrada: Long,
    val dataSaida: Long? = null,
    val status: String = "ENTRADA_REGISTRADA"
)

/**
 * Interface Retrofit para a API REST do backend
 */
interface ApiService {
    @GET("api/owners")
    suspend fun getOwners(): List<OwnerDto>

    @GET("api/owners/{id}")
    suspend fun getOwnerById(@Path("id") id: Long): OwnerDto

    @POST("api/owners")
    suspend fun createOwner(@Body owner: OwnerDto): OwnerDto

    @PUT("api/owners/{id}")
    suspend fun updateOwner(@Path("id") id: Long, @Body owner: OwnerDto): OwnerDto

    @DELETE("api/owners/{id}")
    suspend fun deleteOwner(@Path("id") id: Long): retrofit2.Response<Unit>

    @GET("api/visits")
    suspend fun getVisits(): List<VisitDto>

    @POST("api/visits")
    suspend fun createVisit(@Body visit: VisitDto): VisitDto

    @PUT("api/visits/{id}")
    suspend fun updateVisit(@Path("id") id: Long, @Body visit: VisitDto): VisitDto
}

/**
 * Gerenciador de sincronização com o backend REST.
 *
 * Faz upload dos dados locais para o servidor e download dos dados
 * remotos, unificando as fontes.
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val ownerRepository: OwnerRepository,
    private val visitRepository: VisitRepository
) {

    private var api: ApiService? = null

    init {
        api = buildApi()
    }

    private fun buildApi(): ApiService? {
        return try {
            val serverUrl = getServerUrl()
            if (serverUrl.isNotBlank()) {
                Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService::class.java)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtém a URL do servidor configurada nas preferências.
     * Padrão: http://10.0.2.2:3000 (localhost do emulador Android)
     */
    private fun getServerUrl(): String {
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
        // Recria a API com a nova URL
        recreateApi()
    }

    private fun recreateApi() {
        api = buildApi()
    }

    /**
     * Verifica se o backend está configurado e acessível.
     */
    val isConfigured: Boolean
        get() = api != null

    /**
     * Sincroniza dados do backend para o dispositivo (download).
     */
    suspend fun syncDown(): Boolean {
        val api = api ?: return false
        return try {
            // Baixa moradores do servidor e insere localmente
            val remoteOwners = api.getOwners()
            if (remoteOwners.isNotEmpty()) {
                ownerRepository.deleteAll()
                remoteOwners.forEach { dto ->
                    val owner = Owner(
                        id = dto.id,
                        nome = dto.nome,
                        nomeCondominio = dto.nomeCondominio,
                        endereco = dto.endereco,
                        cep = dto.cep,
                        apartamento = dto.apartamento,
                        telefone = dto.telefone,
                        photoUri = dto.photoUri,
                        qrCodePayload = dto.qrCodePayload,
                        dataCadastro = dto.dataCadastro,
                        isOffline = dto.isOffline,
                        offlineMessage = dto.offlineMessage,
                        offlineUntil = dto.offlineUntil
                    )
                    ownerRepository.insertOwner(owner)
                }
            }

            // Baixa visitas do servidor e insere localmente
            val remoteVisits = api.getVisits()
            if (remoteVisits.isNotEmpty()) {
                visitRepository.clearAll()
                remoteVisits.forEach { dto ->
                    val visit = Visit(
                        id = dto.id,
                        nome = dto.nome,
                        documento = dto.documento,
                        apartamento = dto.apartamento,
                        telefone = dto.telefone,
                        motivo = dto.motivo,
                        dataEntrada = dto.dataEntrada,
                        dataSaida = dto.dataSaida,
                        status = try { VisitStatus.valueOf(dto.status) } catch (e: Exception) { VisitStatus.ENTRADA_REGISTRADA }
                    )
                    visitRepository.insertVisit(visit)
                }
            }

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
        val api = api ?: return false
        return try {
            // Envia moradores locais para o servidor
            val localOwners = ownerRepository.observeAllOwners().first()
            localOwners.forEach { owner ->
                try {
                    // Se o morador já tem ID, tenta atualizar; caso contrário, cria
                    if (owner.id > 0L) {
                        api.updateOwner(
                            owner.id,
                            OwnerDto(
                                nome = owner.nome,
                                nomeCondominio = owner.nomeCondominio,
                                endereco = owner.endereco,
                                cep = owner.cep,
                                apartamento = owner.apartamento,
                                telefone = owner.telefone,
                                photoUri = owner.photoUri,
                                qrCodePayload = owner.qrCodePayload,
                                dataCadastro = owner.dataCadastro,
                                isOffline = owner.isOffline,
                                offlineMessage = owner.offlineMessage,
                                offlineUntil = owner.offlineUntil
                            )
                        )
                    } else {
                        api.createOwner(
                            OwnerDto(
                                nome = owner.nome,
                                nomeCondominio = owner.nomeCondominio,
                                endereco = owner.endereco,
                                cep = owner.cep,
                                apartamento = owner.apartamento,
                                telefone = owner.telefone,
                                photoUri = owner.photoUri,
                                qrCodePayload = owner.qrCodePayload,
                                dataCadastro = owner.dataCadastro,
                                isOffline = owner.isOffline,
                                offlineMessage = owner.offlineMessage,
                                offlineUntil = owner.offlineUntil
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Envia visitas locais para o servidor
            val localVisits = visitRepository.observeAllVisits().first()
            localVisits.forEach { visit ->
                try {
                    api.createVisit(
                        VisitDto(
                            nome = visit.nome,
                            documento = visit.documento,
                            apartamento = visit.apartamento,
                            telefone = visit.telefone,
                            motivo = visit.motivo,
                            dataEntrada = visit.dataEntrada,
                            dataSaida = visit.dataSaida,
                            status = visit.status.name
                        )
                    )
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
}
