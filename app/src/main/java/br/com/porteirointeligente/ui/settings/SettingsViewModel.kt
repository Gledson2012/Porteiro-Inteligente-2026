package br.com.porteirointeligente.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.local.LocalDataStore
import br.com.porteirointeligente.data.repository.AuthRepository
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.data.repository.VisitRepository
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
    private val visitRepository: VisitRepository,
    private val themeManager: ThemeManager,
    private val backupManager: BackupManager,
    private val ownerSelectionManager: OwnerSelectionManager,
    private val localDataStore: LocalDataStore,
    private val authRepository: AuthRepository
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
                val selectedId = ownerSelectionManager.getSelectedOwnerId()
                val current = if (selectedId != null) owners.find { it.id == selectedId }
                              else owners.firstOrNull()
                _owner.value = current
                _selectedOwnerForSettings.value = current?.id
            }
        }
    }

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

    fun performBackup(passphrase: String) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            try {
                backupManager.generateBackupAndShare(passphrase)
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
            val encryptedData = br.com.porteirointeligente.util.OfflineCryptoHelper.encryptOwnerData(
                ownerId = currentOwner.id,
                phone = currentOwner.telefone,
                name = currentOwner.nome,
                isOffline = isOffline,
                offlineMessage = message,
                offlineUntil = until
            ) ?: return@launch
            val newPayload = "https://porteiro-inteligente-2026.vercel.app/scan/$encryptedData"

            ownerRepository.updateOwner(
                currentOwner.copy(
                    isOffline = isOffline,
                    offlineMessage = message,
                    offlineUntil = until,
                    qrCodePayload = newPayload
                )
            )
        }
    }

    fun selecionarOwnerParaConfig(ownerId: Long) {
        _selectedOwnerForSettings.value = ownerId
        _owner.value = _allOwners.value.find { it.id == ownerId }
    }

    fun restoreBackup(uri: android.net.Uri, passphrase: String?) {
        viewModelScope.launch {
            _restoreState.value = RestoreState.Loading
            val success = backupManager.restoreBackup(uri, passphrase)
            if (success) {
                _restoreState.value = RestoreState.Success
            } else {
                _restoreState.value = RestoreState.Error("Falha ao importar o arquivo de backup.")
            }
        }
    }

    fun resetRestoreState() {
        _restoreState.value = RestoreState.Idle
    }

    fun deleteAllData() {
        viewModelScope.launch {
            localDataStore.clearAll()
            authRepository.deleteAccount()
            runCatching { ownerSelectionManager.clearSelection() }
        }
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
