package br.com.porteirointeligente.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<ScannerUiEvent>()
    val uiEvent: SharedFlow<ScannerUiEvent> = _uiEvent

    fun onQrCodeDetected(content: String) {
        viewModelScope.launch {
            var finalUrl: String? = null
            var targetApartment: String? = null

            if (content.startsWith("https://wa.me/")) {
                // Formato legado em texto puro
                finalUrl = content
                val firstOwner = ownerRepository.observeAllOwners().first().firstOrNull()
                targetApartment = firstOwner?.apartamento
            } else if (content.startsWith("https://porteirointeligente.com/scan?data=")) {
                // Novo formato criptografado para privacidade
                val encryptedData = content.substringAfter("https://porteirointeligente.com/scan?data=")
                val decryptedJson = br.com.porteirointeligente.util.CryptoUtil.decrypt(encryptedData)
                if (decryptedJson != null) {
                    try {
                        val json = com.google.gson.JsonParser.parseString(decryptedJson).asJsonObject
                        val phone = json.get("phone").asString
                        val ap = json.get("ap").asString
                        targetApartment = ap
                        finalUrl = "https://wa.me/$phone?text=Olá,%20sou%20o%20entregador%20e%20estou%20na%20portaria."
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (finalUrl == null) {
                _uiEvent.emit(ScannerUiEvent.InvalidQrCode)
                return@launch
            }

            // Busca o morador pelo apartamento do QR Code para verificar status offline
            val owner = if (targetApartment != null) {
                ownerRepository.getOwnerByApartamento(targetApartment)
            } else {
                ownerRepository.observeAllOwners().first().firstOrNull()
            }

            if (owner != null && owner.isCurrentlyOffline()) {
                _uiEvent.emit(ScannerUiEvent.ShowOfflineMessage(owner.offlineMessage, finalUrl))
            } else {
                _uiEvent.emit(ScannerUiEvent.OpenWhatsApp(finalUrl))
            }
        }
    }

    sealed class ScannerUiEvent {
        data class OpenWhatsApp(val url: String) : ScannerUiEvent()
        data class ShowOfflineMessage(val message: String, val url: String) : ScannerUiEvent()
        object InvalidQrCode : ScannerUiEvent()
    }
}
