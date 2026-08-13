package br.com.porteirointeligente.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.util.CryptoUtil
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
            var checkLocalOwnerStatus = false

            when {
                // ============================================================
                // FORMATO QR V2
                // URL com identificador do morador e payload híbrido criptografado
                // Suporta tanto o domínio legado web.app quanto o novo vercel.app
                // ============================================================
                content.startsWith("https://porteiro-inteligente.web.app/scan/") ||
                content.startsWith("https://project-v6x0x.vercel.app/scan/") ||
                content.startsWith("https://porteiro-inteligente-2026.vercel.app/scan/") -> {
                    val idPart = when {
                        content.startsWith("https://porteiro-inteligente.web.app/scan/") -> {
                            content.substringAfter("https://porteiro-inteligente.web.app/scan/")
                        }
                        content.startsWith("https://project-v6x0x.vercel.app/scan/") -> {
                            content.substringAfter("https://project-v6x0x.vercel.app/scan/")
                        }
                        else -> {
                            content.substringAfter("https://porteiro-inteligente-2026.vercel.app/scan/")
                        }
                    }
                    
                    val decryptedJson = br.com.porteirointeligente.util.OfflineCryptoHelper.decryptOwnerData(idPart)
                    if (decryptedJson != null) {
                        // Novo formato encriptado: obtemos tudo diretamente do payload de forma offline
                        val name = decryptedJson.optString("n", "")
                        val rawPhone = decryptedJson.optString("p", "")
                        val offlineUntil = try {
                            decryptedJson.optLong("u", 0L).takeIf { it > 0L }
                        } catch (_: Exception) {
                            null
                        }
                        val isOffline = decryptedJson.optInt("o", 0) == 1 &&
                            (offlineUntil == null || System.currentTimeMillis() < offlineUntil)
                        val offlineMessage = decryptedJson.optString("m", "")
                        
                        val digitsOnly = rawPhone.replace(Regex("\\D"), "")
                        val formattedPhone = if (digitsOnly.startsWith("55")) digitsOnly else "55$digitsOnly"
                        
                        val firstName = name.split(" ").firstOrNull() ?: ""
                        val greeting = if (firstName.isNotBlank()) "Olá $firstName, sou o entregador e estou na portaria." else "Olá, sou o entregador e estou na portaria."
                        val encodedText = java.net.URLEncoder.encode(greeting, "UTF-8")
                        val url = "https://wa.me/$formattedPhone?text=$encodedText"
                        
                        if (isOffline) {
                            _uiEvent.emit(ScannerUiEvent.ShowOfflineMessage(offlineMessage, url))
                        } else {
                            _uiEvent.emit(ScannerUiEvent.OpenWhatsApp(url))
                        }
                        return@launch
                    } else {
                        // Formato legado: busca no banco local pelo ID
                        val ownerId = if (idPart.startsWith("v2.")) {
                            idPart.split('.', limit = 3).getOrNull(1)?.toLongOrNull()
                        } else {
                            idPart.substringBefore("_").toLongOrNull()
                        }
                        if (ownerId != null) {
                            targetOwnerId = ownerId
                            checkLocalOwnerStatus = true
                            val owner = ownerRepository.getOwnerById(ownerId)
                            if (owner != null) {
                                val digitsOnly = owner.telefone.replace(Regex("\\D"), "")
                                val formattedPhone = if (digitsOnly.startsWith("55")) digitsOnly else "55$digitsOnly"
                                finalUrl = "https://wa.me/$formattedPhone?text=Olá,%20sou%20o%20entregador%20e%20estou%20na%20portaria."
                            }
                        } else if (idPart.startsWith("v2.")) {
                            // Outro aparelho pode não ter o morador local. Nesse caso o navegador
                            // fará a descriptografia no backend configurado com QR_PRIVATE_KEY.
                            finalUrl = content
                        } else if (idPart.isNotBlank()) {
                            // QR legado: o app não carrega mais a chave simétrica antiga; o
                            // backend pode tratá-lo enquanto LEGACY_QR_KEY estiver configurada.
                            finalUrl = content
                        }
                    }
                }

                // ============================================================
                // FORMATO LEGADO 1: wa.me (texto puro, sem criptografia)
                // ============================================================
                content.startsWith("https://wa.me/") -> {
                    val validWhatsAppLink = Regex(
                        "^https://wa\\.me/\\d{10,15}(?:\\?.*)?$"
                    ).matches(content)
                    if (validWhatsAppLink) {
                        finalUrl = content
                        targetOwnerId = null // Link direto não é associado a morador local.
                    }
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
                            val rawPhone = json.get("phone").asString
                            val digitsOnly = rawPhone.replace(Regex("\\D"), "")
                            val phone = if (digitsOnly.startsWith("55")) digitsOnly else "55$digitsOnly"
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
            val owner = if (checkLocalOwnerStatus && targetOwnerId != null) {
                ownerRepository.getOwnerById(targetOwnerId)
            } else {
                null
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
