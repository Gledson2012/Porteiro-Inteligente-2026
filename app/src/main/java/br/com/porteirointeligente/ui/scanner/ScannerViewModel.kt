package br.com.porteirointeligente.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.util.CryptoUtil
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository,
    private val cryptoUtil: CryptoUtil
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<ScannerUiEvent>()
    val uiEvent: SharedFlow<ScannerUiEvent> = _uiEvent

    fun onQrCodeDetected(content: String) {
        viewModelScope.launch {
            var finalUrl: String? = null
            var targetOwnerId: Long? = null

            when {
                // ============================================================
                // NOVO FORMATO LGPD (a partir de agora)
                // URL mascarada com apenas o ID do proprietário
                // Ex: https://porteiro-inteligente.web.app/scan/12345_-1234567890
                // ============================================================
                content.startsWith("https://porteiro-inteligente.web.app/scan/") -> {
                    val idPart = content.substringAfter("https://porteiro-inteligente.web.app/scan/")
                    val ownerId = idPart.substringBefore("_").toLongOrNull()
                    if (ownerId != null) {
                        targetOwnerId = ownerId
                        val owner = ownerRepository.getOwnerById(ownerId)
                        if (owner != null) {
                            val formattedPhone = if (owner.telefone.startsWith("55")) owner.telefone else "55${owner.telefone}"
                            finalUrl = "https://wa.me/$formattedPhone?text=Olá,%20sou%20o%20entregador%20e%20estou%20na%20portaria."
                        }
                    }
                }

                // ============================================================
                // FORMATO LEGADO 1: wa.me (texto puro, sem criptografia)
                // ============================================================
                content.startsWith("https://wa.me/") -> {
                    finalUrl = content
                    val firstOwner = ownerRepository.observeAllOwners().first().firstOrNull()
                    targetOwnerId = firstOwner?.id
                }

                // ============================================================
                // FORMATO LEGADO 2: Criptografado (AES/GCM no Android Keystore)
                // Mantido para compatibilidade com QR Codes antigos
                // ============================================================
                content.startsWith("https://porteirointeligente.com/scan?data=") -> {
                    val encryptedData = content.substringAfter("https://porteirointeligente.com/scan?data=")
                    val decryptedJson = cryptoUtil.decrypt(encryptedData)
                    if (decryptedJson != null) {
                        try {
                            val json = JsonParser.parseString(decryptedJson).asJsonObject
                            val phone = json.get("phone").asString
                            finalUrl = "https://wa.me/$phone?text=Olá,%20sou%20o%20entregador%20e%20estou%20na%20portaria."
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            if (finalUrl == null) {
                _uiEvent.emit(ScannerUiEvent.InvalidQrCode)
                return@launch
            }

            // Verifica status offline do proprietário
            val owner = if (targetOwnerId != null) {
                ownerRepository.getOwnerById(targetOwnerId)
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
