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
            val owner = ownerRepository.observeAllOwners().first().firstOrNull()
            if (owner != null && owner.isCurrentlyOffline()) {
                _uiEvent.emit(ScannerUiEvent.ShowOfflineMessage(owner.offlineMessage, content))
            } else {
                _uiEvent.emit(ScannerUiEvent.OpenWhatsApp(content))
            }
        }
    }

    sealed class ScannerUiEvent {
        data class OpenWhatsApp(val url: String) : ScannerUiEvent()
        data class ShowOfflineMessage(val message: String, val url: String) : ScannerUiEvent()
    }
}
