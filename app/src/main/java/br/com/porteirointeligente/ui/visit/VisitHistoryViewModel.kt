package br.com.porteirointeligente.ui.visit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitHistoryViewModel @Inject constructor(
    private val visitRepository: VisitRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(Filter.ALL)
    private val _uiState = MutableStateFlow<VisitHistoryUIState>(VisitHistoryUIState.Loading)
    val uiState: StateFlow<VisitHistoryUIState> = _uiState

    init {
        loadVisits()
    }

    private fun loadVisits() {
        viewModelScope.launch {
            _uiState.value = VisitHistoryUIState.Loading
            try {
                _filter.flatMapLatest { filter ->
                    when (filter) {
                        Filter.ALL -> visitRepository.observeAllVisits()
                        Filter.ACTIVE -> visitRepository.observeVisitsByStatus(VisitStatus.ENTRADA_REGISTRADA)
                    }
                }.collect { visits ->
                    _uiState.value = VisitHistoryUIState.Success(visits, _filter.value)
                }
            } catch (e: Exception) {
                _uiState.value = VisitHistoryUIState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun setFilter(filter: Filter) {
        _filter.value = filter
    }

    fun registrarSaida(visit: Visit) {
        viewModelScope.launch {
            visitRepository.updateVisit(
                visit.copy(
                    dataSaida = System.currentTimeMillis(),
                    status = VisitStatus.SAIDA_REGISTRADA
                )
            )
            // A UI irá se atualizar automaticamente por causa do Flow
        }
    }

    enum class Filter { ALL, ACTIVE }
}

sealed interface VisitHistoryUIState {
    object Loading : VisitHistoryUIState
    data class Success(val visits: List<Visit>, val filter: VisitHistoryViewModel.Filter) : VisitHistoryUIState
    data class Error(val message: String) : VisitHistoryUIState
}

