package br.com.porteirointeligente.ui.visit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import br.com.porteirointeligente.util.OwnerSelectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitRegistrationViewModel @Inject constructor(
    private val visitRepository: VisitRepository,
    private val ownerSelectionManager: OwnerSelectionManager,
    private val ownerRepository: OwnerRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<VisitRegistrationUIState>(VisitRegistrationUIState.Idle)
    val uiState: StateFlow<VisitRegistrationUIState> = _uiState

    fun registrarVisita(
        nome: String,
        documento: String,
        apartamento: String,
        telefone: String,
        motivo: String,
        placa: String = ""
    ) {
        if (nome.isBlank() || apartamento.isBlank()) {
            _uiState.value = VisitRegistrationUIState.Error("Nome e apartamento são obrigatórios.")
            return
        }

        viewModelScope.launch {
            _uiState.value = VisitRegistrationUIState.Loading
            val ownerId = ownerSelectionManager.getSelectedOwnerId()
            val owner = ownerId?.let { ownerRepository?.getOwnerById(it) }

            val visit = Visit(
                ownerId = ownerId,
                nome = nome.trim(),
                documento = documento.trim(),
                apartamento = apartamento.trim(),
                telefone = telefone.trim(),
                motivo = motivo.trim(),
                dataEntrada = System.currentTimeMillis(),
                status = VisitStatus.ENTRADA_REGISTRADA,
                placa = placa.trim().takeIf { it.isNotBlank() }
            )
            try {
                val savedVisit = visitRepository.insertVisit(visit)
                _uiState.value = VisitRegistrationUIState.Success(
                    visit = savedVisit,
                    ownerPhone = owner?.telefone,
                    ownerName = owner?.nome
                )
            } catch (e: Exception) {
                _uiState.value = VisitRegistrationUIState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}

sealed interface VisitRegistrationUIState {
    object Idle : VisitRegistrationUIState
    object Loading : VisitRegistrationUIState
    data class Success(
        val visit: Visit? = null,
        val ownerPhone: String? = null,
        val ownerName: String? = null
    ) : VisitRegistrationUIState
    data class Error(val message: String) : VisitRegistrationUIState
}
