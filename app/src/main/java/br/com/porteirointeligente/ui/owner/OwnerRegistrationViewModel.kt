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
            
            val payload = generateQrPayload(id, nomeCondominio)
            
            val owner = Owner(
                id = id,
                nome = nome.trim(),
                nomeCondominio = nomeCondominio.trim(),
                endereco = endereco.trim(),
                cep = cep.trim(),
                apartamento = apartamento.trim(),
                telefone = telefone.trim(),
                photoUri = photoUri,
                qrCodePayload = payload
            )

            val result = if (id > 0L) {
                ownerRepository.updateOwner(owner)
            } else {
                ownerRepository.insertOwner(owner)
            }

            _uiState.value = if (result.isSuccess) {
                OwnerRegistrationUIState.Success(result.getOrNull()!!)
            } else {
                OwnerRegistrationUIState.Error(result.exceptionOrNull()?.message ?: "Erro desconhecido")
            }
        }
    }
    
    private fun generateQrPayload(ownerId: Long, condominio: String): String {
        val hash = condominio.hashCode().toUInt()
        return "https://porteiro-inteligente.web.app/scan/${ownerId}_$hash"
    }
}

sealed interface OwnerRegistrationUIState {
    object Idle : OwnerRegistrationUIState
    object Loading : OwnerRegistrationUIState
    data class Success(val owner: Owner) : OwnerRegistrationUIState
    data class Error(val message: String) : OwnerRegistrationUIState
}

