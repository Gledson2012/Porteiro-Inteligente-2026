package br.com.porteirointeligente.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerRegistrationViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OwnerRegistrationUIState>(OwnerRegistrationUIState.Idle)
    val uiState: StateFlow<OwnerRegistrationUIState> = _uiState

    suspend fun loadOwner(id: Long): Owner? =
        ownerRepository.getOwnerById(id)

    fun registerOwner(id: Long = 0L, nome: String, nomeCondominio: String, endereco: String, cep: String, apartamento: String, telefone: String, photoUri: String?) {
        if (nome.isBlank() || endereco.isBlank() || telefone.isBlank()) {
            _uiState.value = OwnerRegistrationUIState.Error("Nome, endereço e telefone são obrigatórios.")
            return
        }

        viewModelScope.launch {
            _uiState.value = OwnerRegistrationUIState.Loading
            
            val existing = if (id > 0L) ownerRepository.getOwnerById(id) else null
            val isOffline = existing?.isOffline ?: false
            val offlineMsg = existing?.offlineMessage ?: ""
            val offlineUntil = existing?.offlineUntil

            val owner = Owner(
                id = id,
                nome = nome.trim(),
                nomeCondominio = nomeCondominio.trim(),
                endereco = endereco.trim(),
                cep = cep.trim(),
                apartamento = apartamento.trim(),
                telefone = telefone.trim(),
                photoUri = photoUri,
                // Um novo morador ainda não possui ID. O QR será preenchido logo após o INSERT.
                qrCodePayload = existing?.qrCodePayload ?: "",
                isOffline = isOffline,
                offlineMessage = offlineMsg,
                offlineUntil = offlineUntil,
                dataCadastro = existing?.dataCadastro ?: System.currentTimeMillis()
            )

            var insertedOwner: Owner? = null
            try {
                val savedOwner = if (id > 0L) {
                    val encryptedData = br.com.porteirointeligente.util.OfflineCryptoHelper.encryptOwnerData(
                        ownerId = id,
                        phone = telefone.trim(),
                        name = nome.trim(),
                        isOffline = isOffline,
                        offlineMessage = offlineMsg,
                        offlineUntil = offlineUntil
                    ) ?: throw IllegalStateException("Não foi possível gerar o QR Code.")
                    val updatedOwner = owner.copy(
                        qrCodePayload = "https://porteiro-inteligente-2026.vercel.app/scan/$encryptedData"
                    )
                    ownerRepository.updateOwner(updatedOwner)
                    updatedOwner
                } else {
                    val createdOwner = ownerRepository.insertOwner(owner)
                    insertedOwner = createdOwner
                    if (createdOwner.id <= 0L) {
                        throw IllegalStateException("Não foi possível salvar o morador.")
                    }
                    val encryptedData = br.com.porteirointeligente.util.OfflineCryptoHelper.encryptOwnerData(
                        ownerId = createdOwner.id,
                        phone = telefone.trim(),
                        name = nome.trim(),
                        isOffline = isOffline,
                        offlineMessage = offlineMsg,
                        offlineUntil = offlineUntil
                    ) ?: throw IllegalStateException("Não foi possível gerar o QR Code.")
                    val updatedOwner = createdOwner.copy(
                        qrCodePayload = "https://porteiro-inteligente-2026.vercel.app/scan/$encryptedData"
                    )
                    ownerRepository.updateOwner(updatedOwner)
                    updatedOwner
                }
                _uiState.value = OwnerRegistrationUIState.Success(savedOwner)
            } catch (e: Exception) {
                insertedOwner?.let { runCatching { ownerRepository.deleteOwner(it) } }
                _uiState.value = OwnerRegistrationUIState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}

sealed interface OwnerRegistrationUIState {
    object Idle : OwnerRegistrationUIState
    object Loading : OwnerRegistrationUIState
    data class Success(val owner: Owner) : OwnerRegistrationUIState
    data class Error(val message: String) : OwnerRegistrationUIState
}
