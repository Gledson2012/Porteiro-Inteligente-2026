package br.com.porteirointeligente.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.util.OwnerSelectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val visitRepository: VisitRepository,
    private val ownerRepository: OwnerRepository,
    private val ownerSelectionManager: OwnerSelectionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Loading)
    val uiState: StateFlow<HomeUIState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUIState.Loading
            try {
                ownerRepository.observeAllOwners()
                    .combine(visitRepository.observeAllVisits()) { owners, visits ->
                        if (ownerSelectionManager.getSelectedOwnerId() == null && owners.isNotEmpty()) {
                            ownerSelectionManager.selectOwner(owners.first().id)
                        }

                        val selectedId = ownerSelectionManager.selectedOwnerId.first()
                        val selectedOwner = if (selectedId != null) {
                            owners.find { it.id == selectedId }
                        } else {
                            owners.firstOrNull()
                        }

                        HomeUIState.Success(
                            allOwners = owners,
                            selectedOwner = selectedOwner,
                            recentVisits = visits.take(5)
                        )
                    }.collect { successState ->
                        _uiState.value = successState
                    }
            } catch (e: Exception) {
                _uiState.value = HomeUIState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun selecionarMorador(ownerId: Long) {
        viewModelScope.launch {
            ownerSelectionManager.selectOwner(ownerId)
            // Recarregar os dados para refletir a nova seleção
            loadData()
        }
    }
}

sealed interface HomeUIState {
    object Loading : HomeUIState
    data class Success(
        val allOwners: List<Owner>,
        val selectedOwner: Owner?,
        val recentVisits: List<Visit>
    ) : HomeUIState
    data class Error(val message: String) : HomeUIState
}

