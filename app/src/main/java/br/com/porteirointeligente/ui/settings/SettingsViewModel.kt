package br.com.porteirointeligente.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.util.AppTheme
import br.com.porteirointeligente.util.BackupManager
import br.com.porteirointeligente.util.OwnerSelectionManager
import br.com.porteirointeligente.util.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository,
    private val themeManager: ThemeManager,
    private val backupManager: BackupManager,
    private val ownerSelectionManager: OwnerSelectionManager
) : ViewModel() {

    val themeState: StateFlow<AppTheme> = themeManager.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.SYSTEM
        )

    val dynamicColorState: StateFlow<Boolean> = themeManager.dynamicColorFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private val _owner = MutableStateFlow<Owner?>(null)
    val owner: StateFlow<Owner?> = _owner

    private val _allOwners = MutableStateFlow<List<Owner>>(emptyList())
    val allOwners: StateFlow<List<Owner>> = _allOwners

    /** ID do morador selecionado para configurações */
    private val _selectedOwnerForSettings = MutableStateFlow<Long?>(null)

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState

    private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
    val restoreState: StateFlow<RestoreState> = _restoreState

    init {
        loadOwners()
    }

    private fun loadOwners() {
        viewModelScope.launch {
            ownerRepository.observeAllOwners().collect { owners ->
                _allOwners.value = owners
                // Sincroniza com o OwnerSelectionManager
                val selectedId = ownerSelectionManager.getSelectedOwnerId()
                val current = if (selectedId != null) owners.find { it.id == selectedId }
                              else owners.firstOrNull()
                _owner.value = current
                _selectedOwnerForSettings.value = current?.id
            }
        }
    }

    /** Troca o morador ativo das configurações */
    fun selecionarMorador(ownerId: Long) {
        viewModelScope.launch {
            ownerSelectionManager.selectOwner(ownerId)
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            themeManager.setTheme(theme)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDynamicColor(enabled)
        }
    }

    fun performBackup() {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            try {
                backupManager.generateBackupAndShare()
                _backupState.value = BackupState.Success
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Erro ao gerar backup")
            }
        }
    }

    fun resetBackupState() {
        _backupState.value = BackupState.Idle
    }

    fun updateOfflineStatus(
        isOffline: Boolean,
        message: String,
        durationMillis: Long?
    ) {
        val currentOwner = _owner.value ?: return
        
        val until = if (durationMillis != null) {
            System.currentTimeMillis() + durationMillis
        } else {
            null
        }

        viewModelScope.launch {
            ownerRepository.updateOwner(
                currentOwner.copy(
                    isOffline = isOffline,
                    offlineMessage = message,
                    offlineUntil = until
                )
            )
        }
    }

    /** Seleciona um morador para visualizar/editar configurações */
    fun selecionarOwnerParaConfig(ownerId: Long) {
        _selectedOwnerForSettings.value = ownerId
        _owner.value = _allOwners.value.find { it.id == ownerId }
    }

    fun restoreBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            _restoreState.value = RestoreState.Loading
            val success = backupManager.restoreBackup(uri)
            if (success) {
                _restoreState.value = RestoreState.Success
                loadOwners() // Recarrega os moradores no ViewModel
            } else {
                _restoreState.value = RestoreState.Error("Falha ao importar o arquivo de backup.")
            }
        }
    }

    fun resetRestoreState() {
        _restoreState.value = RestoreState.Idle
    }

    sealed class BackupState {
        object Idle : BackupState()
        object Loading : BackupState()
        object Success : BackupState()
        data class Error(val message: String) : BackupState()
    }

    sealed class RestoreState {
        object Idle : RestoreState()
        object Loading : RestoreState()
        object Success : RestoreState()
        data class Error(val message: String) : RestoreState()
    }
}
