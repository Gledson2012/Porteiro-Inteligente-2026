package br.com.porteirointeligente.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.OwnerRepository
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.util.AppTheme
import br.com.porteirointeligente.util.BackupManager
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
    private val backupManager: BackupManager
) : ViewModel() {

    val themeState: StateFlow<AppTheme> = themeManager.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.SYSTEM
        )

    private val _owner = MutableStateFlow<Owner?>(null)
    val owner: StateFlow<Owner?> = _owner

    init {
        loadOwner()
    }

    private fun loadOwner() {
        viewModelScope.launch {
            ownerRepository.observeAllOwners().collect { owners ->
                _owner.value = owners.firstOrNull()
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            themeManager.setTheme(theme)
        }
    }

    fun performBackup() {
        viewModelScope.launch {
            backupManager.generateBackupAndShare()
        }
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
            null // Para sempre
        }

        viewModelScope.launch {
            ownerRepository.insertOwner(
                currentOwner.copy(
                    isOffline = isOffline,
                    offlineMessage = message,
                    offlineUntil = until
                )
            )
        }
    }
}
