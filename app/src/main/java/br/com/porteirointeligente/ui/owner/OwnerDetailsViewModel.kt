package br.com.porteirointeligente.ui.owner

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.util.QrCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para a tela de exibição do QR Code do morador.
 */
@HiltViewModel
class OwnerDetailsViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OwnerDetailsUiState>(OwnerDetailsUiState.Loading)
    val uiState: StateFlow<OwnerDetailsUiState> = _uiState

    fun loadOwner() {
        viewModelScope.launch {
            ownerRepository.observeAllOwners().collect { owners ->
                if (owners.isNotEmpty()) {
                    val owner = owners.first()
                    val qrCode = QrCodeGenerator.generate(owner.qrCodePayload)
                    _uiState.value = OwnerDetailsUiState.Success(owner, qrCode)
                } else {
                    _uiState.value = OwnerDetailsUiState.Empty
                }
            }
        }
    }

    fun deleteOwner() {
        viewModelScope.launch {
            ownerRepository.deleteAll()
        }
    }

    sealed class OwnerDetailsUiState {
        object Loading : OwnerDetailsUiState()
        object Empty : OwnerDetailsUiState()
        data class Success(val owner: Owner, val qrCode: Bitmap?) : OwnerDetailsUiState()
    }
}
