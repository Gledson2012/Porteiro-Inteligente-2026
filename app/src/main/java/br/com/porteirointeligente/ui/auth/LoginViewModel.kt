package br.com.porteirointeligente.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.porteirointeligente.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.login(username, password)
            _loginState.value = if (result.isSuccess) {
                LoginState.Success
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: "Erro desconhecido")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
