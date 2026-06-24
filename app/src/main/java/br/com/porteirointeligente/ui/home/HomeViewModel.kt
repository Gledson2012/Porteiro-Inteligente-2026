package br.com.porteirointeligente.ui.home

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.util.OwnerSelectionManager
import br.com.porteirointeligente.util.QrCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel da tela inicial (Home).
 * Agora suporta múltiplos moradores com seleção ativa.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val visitRepository: VisitRepository,
    private val ownerRepository: OwnerRepository,
    private val ownerSelectionManager: OwnerSelectionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Lista completa de moradores para o seletor */
    val todosMoradores: StateFlow<List<Owner>> = ownerRepository
        .observeAllOwners()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            val owners = ownerRepository.observeAllOwners().first()
            // Se não houver morador selecionado, seleciona o primeiro
            if (ownerSelectionManager.getSelectedOwnerId() == null && owners.isNotEmpty()) {
                ownerSelectionManager.selectOwner(owners.first().id)
            }
            _isLoading.value = false
        }
    }

    private val _condominioPadrao = MutableStateFlow("Condomínio")
    private val _apartamentoPadrao = MutableStateFlow("Unidade")

    fun configurarIdentificacao(condominio: String, apartamento: String) {
        _condominioPadrao.value = condominio
        _apartamentoPadrao.value = apartamento
    }

    /** Morador atualmente selecionado */
    @OptIn(ExperimentalCoroutinesApi::class)
    val moradorSelecionado: StateFlow<Owner?> = ownerSelectionManager.selectedOwnerId
        .flatMapLatest { selectedId ->
            ownerRepository.observeAllOwners().map { owners ->
                if (selectedId != null) owners.find { it.id == selectedId }
                else owners.firstOrNull()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = null
        )

    val condominio: StateFlow<String> = combine(
        moradorSelecionado,
        _condominioPadrao
    ) { morador, padrao ->
        morador?.nomeCondominio ?: padrao
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = "Carregando..."
    )

    val apartamento: StateFlow<String> = combine(
        moradorSelecionado,
        _apartamentoPadrao
    ) { morador, padrao ->
        morador?.apartamento ?: padrao
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = "Carregando..."
    )

    val visitasRecentes: StateFlow<List<Visit>> = visitRepository
        .observeAllVisits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    /** Estatísticas de visitas */
    val statsTotalHoje: StateFlow<Int> = visitasRecentes
        .map { visits ->
            val hoje = java.util.Calendar.getInstance()
            hoje.set(java.util.Calendar.HOUR_OF_DAY, 0)
            hoje.set(java.util.Calendar.MINUTE, 0)
            hoje.set(java.util.Calendar.SECOND, 0)
            hoje.set(java.util.Calendar.MILLISECOND, 0)
            val inicioHoje = hoje.timeInMillis
            visits.count { it.dataEntrada >= inicioHoje }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = 0
        )

    val statsEntradasHoje: StateFlow<Int> = visitasRecentes
        .map { visits ->
            val hoje = java.util.Calendar.getInstance()
            hoje.set(java.util.Calendar.HOUR_OF_DAY, 0)
            hoje.set(java.util.Calendar.MINUTE, 0)
            hoje.set(java.util.Calendar.SECOND, 0)
            hoje.set(java.util.Calendar.MILLISECOND, 0)
            val inicioHoje = hoje.timeInMillis
            visits.count { it.dataEntrada >= inicioHoje && it.status == br.com.porteirointeligente.domain.model.VisitStatus.ENTRADA_REGISTRADA }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = 0
        )

    val statsVisitantesUnicos: StateFlow<Int> = visitasRecentes
        .map { visits ->
            val hoje = java.util.Calendar.getInstance()
            hoje.set(java.util.Calendar.HOUR_OF_DAY, 0)
            hoje.set(java.util.Calendar.MINUTE, 0)
            hoje.set(java.util.Calendar.SECOND, 0)
            hoje.set(java.util.Calendar.MILLISECOND, 0)
            val inicioHoje = hoje.timeInMillis
            visits.filter { it.dataEntrada >= inicioHoje }.distinctBy { it.nome }.count()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = 0
        )

    /** Mantido para compatibilidade, mas use [moradorSelecionado] */
    val moradorCadastrado: StateFlow<Owner?> = moradorSelecionado

    val qrCodeMorador: StateFlow<Bitmap?> = moradorSelecionado
        .map { it?.let { owner -> QrCodeGenerator.generate(owner.qrCodePayload) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = null
        )

    /** Troca o morador ativo */
    fun selecionarMorador(ownerId: Long) {
        viewModelScope.launch {
            ownerSelectionManager.selectOwner(ownerId)
        }
    }
}
