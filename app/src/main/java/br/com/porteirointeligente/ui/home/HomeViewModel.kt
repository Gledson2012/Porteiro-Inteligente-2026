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
        // Auto-seleciona o primeiro morador se nenhum estiver selecionado
        viewModelScope.launch {
            val selectedId = ownerSelectionManager.getSelectedOwnerId()
            if (selectedId == null) {
                val owners = ownerRepository.observeAllOwners().first()
                if (owners.isNotEmpty()) {
                    ownerSelectionManager.selectOwner(owners.first().id)
                }
            }
        }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUIState.Loading
            try {
                combine(
                    ownerRepository.observeAllOwners(),
                    visitRepository.observeAllVisits(),
                    ownerSelectionManager.selectedOwnerId
                ) { owners, visits, selectedId ->
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

    /**
     * Seleciona um morador. O combine reativo com [ownerSelectionManager.selectedOwnerId]
     * garante que a UI será atualizada automaticamente.
     */
    fun selecionarMorador(ownerId: Long) {
        viewModelScope.launch {
            ownerSelectionManager.selectOwner(ownerId)
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
