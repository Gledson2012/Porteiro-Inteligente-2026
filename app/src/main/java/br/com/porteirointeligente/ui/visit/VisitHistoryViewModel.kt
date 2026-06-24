package br.com.porteirointeligente.ui.visit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitHistoryViewModel @Inject constructor(
    private val visitRepository: VisitRepository,
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _filter = MutableStateFlow<Filter>(Filter.ALL)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _allOwners = MutableStateFlow<List<Owner>>(emptyList())
    val allOwners: StateFlow<List<Owner>> = _allOwners

    init {
        viewModelScope.launch {
            visitRepository.observeAllVisits().first()
            ownerRepository.observeAllOwners().collect { owners ->
                _allOwners.value = owners
            }
            _isLoading.value = false
        }
    }

    /** Morador mais antigo para compatibilidade com a UI existente */
    val owner: StateFlow<Owner?> = ownerRepository
        .observeAllOwners()
        .map { it.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val visits: StateFlow<List<Visit>> = _filter
        .flatMapLatest { filter ->
            when (filter) {
                Filter.ALL -> visitRepository.observeAllVisits()
                Filter.ACTIVE -> visitRepository.observeVisitsByStatus(VisitStatus.ENTRADA_REGISTRADA)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

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
        }
    }

    enum class Filter { ALL, ACTIVE }
}
