package br.com.porteirointeligente.ui.visit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitRegistrationViewModel @Inject constructor(
    private val visitRepository: VisitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<VisitRegistrationUIState>(VisitRegistrationUIState.Idle)
    val uiState: StateFlow<VisitRegistrationUIState> = _uiState

    fun registrarVisita(
        nome: String,
        documento: String,
        apartamento: String,
        telefone: String,
        motivo: String
    ) {
        if (nome.isBlank() || apartamento.isBlank()) {
            _uiState.value = VisitRegistrationUIState.Error("Nome e apartamento são obrigatórios.")
            return
        }

        viewModelScope.launch {
            _uiState.value = VisitRegistrationUIState.Loading
            val visit = Visit(
                nome = nome.trim(),
                documento = documento.trim(),
                apartamento = apartamento.trim(),
                telefone = telefone.trim(),
                motivo = motivo.trim(),
                dataEntrada = System.currentTimeMillis(),
                status = VisitStatus.ENTRADA_REGISTRADA
            )
            try {
                visitRepository.insertVisit(visit)
                _uiState.value = VisitRegistrationUIState.Success
            } catch (e: Exception) {
                _uiState.value = VisitRegistrationUIState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}

sealed interface VisitRegistrationUIState {
    object Idle : VisitRegistrationUIState
    object Loading : VisitRegistrationUIState
    object Success : VisitRegistrationUIState
    data class Error(val message: String) : VisitRegistrationUIState
}

