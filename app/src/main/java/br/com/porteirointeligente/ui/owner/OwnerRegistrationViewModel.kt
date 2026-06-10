package br.com.porteirointeligente.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para o cadastro de moradores com mais detalhes.
 */
@HiltViewModel
class OwnerRegistrationViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _registrationEvent = MutableSharedFlow<RegistrationUiEvent>()
    val registrationEvent = _registrationEvent.asSharedFlow()

    fun registerOwner(id: Long = 0L, nome: String, nomeCondominio: String, endereco: String, cep: String, apartamento: String, telefone: String, photoUri: String?) {
        if (nome.isBlank() || endereco.isBlank() || apartamento.isBlank() || telefone.isBlank()) {
            viewModelScope.launch {
                _registrationEvent.emit(RegistrationUiEvent.ErrorFields)
            }
            return
        }

        val cleanPhone = telefone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length < 10) {
            viewModelScope.launch {
                _registrationEvent.emit(RegistrationUiEvent.ErrorPhone)
            }
            return
        }

        viewModelScope.launch {
            val formattedPhone = if (cleanPhone.startsWith("55")) cleanPhone else "55$cleanPhone"
            val payload = "https://wa.me/$formattedPhone?text=Olá,%20sou%20o%20entregador%20e%20estou%20na%20portaria."
            
            val owner = Owner(
                id = id,
                nome = nome.trim(),
                nomeCondominio = nomeCondominio.trim(),
                endereco = endereco.trim(),
                cep = cep.trim(),
                apartamento = apartamento.trim(),
                telefone = cleanPhone,
                photoUri = photoUri,
                qrCodePayload = payload
            )
            ownerRepository.insertOwner(owner)
            _registrationEvent.emit(RegistrationUiEvent.Success)
        }
    }

    sealed class RegistrationUiEvent {
        object Success : RegistrationUiEvent()
        object ErrorFields : RegistrationUiEvent()
        object ErrorPhone : RegistrationUiEvent()
    }
}
