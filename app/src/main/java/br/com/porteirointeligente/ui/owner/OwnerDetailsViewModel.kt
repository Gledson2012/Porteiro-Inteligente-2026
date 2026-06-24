package br.com.porteirointeligente.ui.owner

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.util.OwnerSelectionManager
import br.com.porteirointeligente.util.PhotoSaver
import br.com.porteirointeligente.util.QrCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para a tela de exibição do QR Code do morador.
 * Agora suporta múltiplos moradores com navegação entre eles.
 */
@HiltViewModel
class OwnerDetailsViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository,
    private val ownerSelectionManager: OwnerSelectionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<OwnerDetailsUiState>(OwnerDetailsUiState.Loading)
    val uiState: StateFlow<OwnerDetailsUiState> = _uiState

    /** Lista de todos os moradores para navegação */
    private val _todosOwners = MutableStateFlow<List<Owner>>(emptyList())

    init {
        loadOwners()
    }

    private fun loadOwners() {
        viewModelScope.launch {
            combine(
                ownerRepository.observeAllOwners(),
                ownerSelectionManager.selectedOwnerId
            ) { owners, selectedId -> owners to selectedId }
                .collectLatest { (owners, selectedId) ->
                    _todosOwners.value = owners
                    updateUiState(owners, selectedId)
                }
        }
    }

    private fun updateUiState(owners: List<Owner>, selectedId: Long?) {
        val owner = if (selectedId != null) owners.find { it.id == selectedId }
                    else owners.firstOrNull()

        if (owner != null) {
            val qrCode = QrCodeGenerator.generate(owner.qrCodePayload)
            _uiState.value = OwnerDetailsUiState.Success(owner, qrCode, owners)
        } else if (owners.isNotEmpty()) {
            // Se o selecionado não existe mais, pega o primeiro
            val firstOwner = owners.first()
            viewModelScope.launch {
                ownerSelectionManager.selectOwner(firstOwner.id)
            }
            val qrCode = QrCodeGenerator.generate(firstOwner.qrCodePayload)
            _uiState.value = OwnerDetailsUiState.Success(firstOwner, qrCode, owners)
        } else {
            _uiState.value = OwnerDetailsUiState.Empty
        }
    }

    fun selecionarOwner(ownerId: Long) {
        viewModelScope.launch {
            ownerSelectionManager.selectOwner(ownerId)
        }
    }

    fun deleteOwner(ownerId: Long) {
        viewModelScope.launch {
            val owner = ownerRepository.getOwnerById(ownerId)
            if (owner != null) {
                // Remove a foto do armazenamento interno antes de deletar
                owner.photoUri?.let { PhotoSaver.deletePhoto(context, it) }
                ownerRepository.deleteOwner(owner)
            }
        }
    }

    sealed class OwnerDetailsUiState {
        object Loading : OwnerDetailsUiState()
        object Empty : OwnerDetailsUiState()
        data class Success(
            val owner: Owner,
            val qrCode: Bitmap?,
            val allOwners: List<Owner> = emptyList()
        ) : OwnerDetailsUiState()
    }
}
