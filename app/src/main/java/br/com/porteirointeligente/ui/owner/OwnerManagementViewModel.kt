package br.com.porteirointeligente.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerManagementViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OwnerUIState>(OwnerUIState.Loading)
    val uiState: StateFlow<OwnerUIState> = _uiState

    init {
        loadOwners()
    }

    fun loadOwners() {
        viewModelScope.launch {
            _uiState.value = OwnerUIState.Loading
            try {
                ownerRepository.observeAllOwners().collect { owners ->
                    _uiState.value = OwnerUIState.Success(owners)
                }
            } catch (e: Exception) {
                _uiState.value = OwnerUIState.Error(e.message ?: "Erro ao carregar moradores")
            }
        }
    }

    fun deleteOwner(owner: Owner) {
        viewModelScope.launch {
            try {
                ownerRepository.deleteOwner(owner)
            } catch (e: Exception) {
                _uiState.value = OwnerUIState.Error(e.message ?: "Erro ao excluir morador")
            }
        }
    }
}

sealed interface OwnerUIState {
    object Loading : OwnerUIState
    data class Success(val owners: List<Owner>) : OwnerUIState
    data class Error(val message: String) : OwnerUIState
}

