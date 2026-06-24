package br.com.porteirointeligente.ui.visit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import br.com.porteirointeligente.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para o registro de visitas com validação aprimorada
 * e notificação local ao registrar entrada.
 */
@HiltViewModel
class VisitRegistrationViewModel @Inject constructor(
    private val visitRepository: VisitRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _event = MutableSharedFlow<VisitUiEvent>()
    val event = _event.asSharedFlow()

    fun registrarVisita(
        nome: String,
        documento: String,
        apartamento: String,
        telefone: String,
        motivo: String
    ) {
        // Validação de campos obrigatórios: Nome e Apartamento
        if (nome.isBlank() || apartamento.isBlank()) {
            viewModelScope.launch {
                _event.emit(VisitUiEvent.ErrorFields)
            }
            return
        }

        viewModelScope.launch {
            val visit = Visit(
                nome = nome.trim(),
                documento = documento.trim(),
                apartamento = apartamento.trim(),
                telefone = telefone.trim(),
                motivo = motivo.trim(),
                dataEntrada = System.currentTimeMillis(),
                status = VisitStatus.ENTRADA_REGISTRADA
            )
            visitRepository.insertVisit(visit)

            // Mostra notificação local sobre a nova visita
            notificationHelper.showVisitNotification(
                visitanteNome = nome.trim(),
                apartamento = apartamento.trim()
            )

            _event.emit(VisitUiEvent.Success)
        }
    }

    sealed class VisitUiEvent {
        object Success : VisitUiEvent()
        object ErrorFields : VisitUiEvent()
    }
}
