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
import java.util.Calendar
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
            val owners = ownerRepository.observeAllOwners().first()
            if (owners.isNotEmpty() && (selectedId == null || owners.none { it.id == selectedId })) {
                ownerSelectionManager.selectOwner(owners.first().id)
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

                    // Visitas antigas sem ownerId são mantidas para o único morador legado.
                    val visitsForOwner = if (selectedOwner != null) {
                        visits.filter {
                            it.ownerId == selectedOwner.id ||
                                (it.ownerId == null && owners.size == 1)
                        }
                    } else {
                        visits
                    }
                    val startOfDay = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val startOfDayMillis = startOfDay.timeInMillis
                    val endOfDay = (startOfDay.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }.timeInMillis

                    HomeUIState.Success(
                        allOwners = owners,
                        selectedOwner = selectedOwner,
                        recentVisits = visitsForOwner.take(5),
                        totalVisitsToday = visitsForOwner.count {
                            it.dataEntrada in startOfDayMillis until endOfDay
                        },
                        activeVisitsCount = visitsForOwner.count {
                            it.status == br.com.porteirointeligente.domain.model.VisitStatus.ENTRADA_REGISTRADA
                        }
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

    fun setOnline() {
        viewModelScope.launch {
            val selectedId = ownerSelectionManager.getSelectedOwnerId() ?: return@launch
            val owner = ownerRepository.getOwnerById(selectedId) ?: return@launch
            
            val encryptedData = br.com.porteirointeligente.util.OfflineCryptoHelper.encryptOwnerData(
                ownerId = owner.id,
                phone = owner.telefone,
                name = owner.nome,
                isOffline = false,
                offlineMessage = "",
                offlineUntil = null
            ) ?: return@launch
            val newPayload = "https://porteiro-inteligente-2026.vercel.app/scan/$encryptedData"

            ownerRepository.updateOwner(
                owner.copy(
                    isOffline = false,
                    offlineUntil = null,
                    offlineMessage = "",
                    qrCodePayload = newPayload
                )
            )
        }
    }
}

sealed interface HomeUIState {
    object Loading : HomeUIState
    data class Success(
        val allOwners: List<Owner>,
        val selectedOwner: Owner?,
        val recentVisits: List<Visit>,
        val totalVisitsToday: Int = 0,
        val activeVisitsCount: Int = 0
    ) : HomeUIState
    data class Error(val message: String) : HomeUIState
}
