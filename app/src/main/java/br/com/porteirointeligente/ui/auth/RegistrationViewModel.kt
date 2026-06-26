package br.com.porteirointeligente.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.network.dto.RegisterRequest
import br.com.porteirointeligente.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            val result = authRepository.register(RegisterRequest(username, password))
            _registrationState.value = if (result.isSuccess) {
                RegistrationState.Success
            } else {
                RegistrationState.Error(result.exceptionOrNull()?.message ?: "Erro desconhecido")
            }
        }
    }
}

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    object Success : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}
