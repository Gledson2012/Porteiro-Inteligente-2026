package br.com.porteirointeligente.ui.owner

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.util.CryptoUtil
import br.com.porteirointeligente.util.PhotoSaver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerRegistrationViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository,
    private val cryptoUtil: CryptoUtil,
    @ApplicationContext private val context: Context
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

        val cleanCep = cep.replace(Regex("[^0-9]"), "")
        if (cleanCep.isNotEmpty() && cleanCep.length != 8) {
            viewModelScope.launch {
                _registrationEvent.emit(RegistrationUiEvent.ErrorCep)
            }
            return
        }

        viewModelScope.launch {
            val persistedPhotoUri = if (photoUri != null && photoUri.startsWith("content://")) {
                PhotoSaver.savePhotoToInternalStorage(context, Uri.parse(photoUri))
            } else {
                photoUri
            }

            val formattedPhone = if (cleanPhone.startsWith("55")) cleanPhone else "55$cleanPhone"

            // Cria um payload JSON estruturado com as informações necessárias do morador
            val cleanName = nome.trim()
            val cleanCondo = nomeCondominio.trim()
            val cleanAp = apartamento.trim()
            val cleanAddress = endereco.trim()

            val jsonPayload = """{"phone":"$formattedPhone","name":"$cleanName","ap":"$cleanAp","condo":"$cleanCondo"}"""
            val encryptedPayload = cryptoUtil.encrypt(jsonPayload)

            // O payload final é uma URL amigável que contém o dado criptografado.
            // Se lida fora do app, direciona para o site institucional. Se lida no app, decifra o morador.
            val payload = if (encryptedPayload != null) {
                "https://porteirointeligente.com/scan?data=$encryptedPayload"
            } else {
                "https://wa.me/$formattedPhone?text=Olá,%20sou%20o%20entregador%20e%20estou%20na%20portaria."
            }

            val owner = Owner(
                id = id,
                nome = cleanName,
                nomeCondominio = cleanCondo,
                endereco = cleanAddress,
                cep = cleanCep,
                apartamento = cleanAp,
                telefone = cleanPhone,
                photoUri = persistedPhotoUri,
                qrCodePayload = payload
            )

            if (id > 0L) {
                ownerRepository.updateOwner(owner)
            } else {
                ownerRepository.insertOwner(owner)
            }
            _registrationEvent.emit(RegistrationUiEvent.Success)
        }
    }

    sealed class RegistrationUiEvent {
        object Success : RegistrationUiEvent()
        object ErrorFields : RegistrationUiEvent()
        object ErrorPhone : RegistrationUiEvent()
        object ErrorCep : RegistrationUiEvent()
    }
}
