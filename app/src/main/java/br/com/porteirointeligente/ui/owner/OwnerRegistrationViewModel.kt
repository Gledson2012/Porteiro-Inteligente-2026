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

            val encryptedData = br.com.porteirointeligente.util.OfflineCryptoHelper.encryptOwnerData(
                phone = telefone.trim(),
                name = nome.trim(),
                isOffline = isOffline,
                offlineMessage = offlineMsg
            ) ?: ""
            val payload = "https://porteiro-inteligente-2026.vercel.app/scan/$encryptedData"

            val owner = Owner(
                id = id,
                nome = nome.trim(),
                nomeCondominio = nomeCondominio.trim(),
                endereco = endereco.trim(),
                cep = cep.trim(),
                apartamento = apartamento.trim(),
                telefone = telefone.trim(),
                photoUri = photoUri,
                qrCodePayload = payload,
                isOffline = isOffline,
                offlineMessage = offlineMsg,
                offlineUntil = offlineUntil
            )

            try {
                val savedOwner = if (id > 0L) {
                    ownerRepository.updateOwner(owner)
                    owner
                } else {
                    ownerRepository.insertOwner(owner)
                }
                _uiState.value = OwnerRegistrationUIState.Success(savedOwner)
            } catch (e: Exception) {
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

