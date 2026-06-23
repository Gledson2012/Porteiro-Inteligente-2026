package br.com.porteirointeligente.ui.home

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.util.QrCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel da tela inicial (Home).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val visitRepository: VisitRepository,
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            ownerRepository.observeAllOwners().first()
            _isLoading.value = false
        }
    }

    private val _condominioPadrao = MutableStateFlow("Condomínio")
    private val _apartamentoPadrao = MutableStateFlow("Unidade")

    /**
     * Configura os nomes padrão para exibição caso não haja morador cadastrado.
     */
    fun configurarIdentificacao(condominio: String, apartamento: String) {
        _condominioPadrao.value = condominio
        _apartamentoPadrao.value = apartamento
    }

    val condominio: StateFlow<String> = combine(
        ownerRepository.observeAllOwners(),
        _condominioPadrao
    ) { owners, padrao ->
        owners.firstOrNull()?.nomeCondominio ?: padrao
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = "Carregando..."
    )

    val apartamento: StateFlow<String> = combine(
        ownerRepository.observeAllOwners(),
        _apartamentoPadrao
    ) { owners, padrao ->
        owners.firstOrNull()?.apartamento ?: padrao
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

    val moradorCadastrado: StateFlow<Owner?> = ownerRepository
        .observeAllOwners()
        .map { it.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = null
        )

    val qrCodeMorador: StateFlow<Bitmap?> = moradorCadastrado
        .map { it?.let { owner -> QrCodeGenerator.generate(owner.qrCodePayload) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = null
        )
}
