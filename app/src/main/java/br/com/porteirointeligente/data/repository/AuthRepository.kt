package br.com.porteirointeligente.data.repository

import br.com.porteirointeligente.BuildConfig
import br.com.porteirointeligente.data.network.ApiService
import br.com.porteirointeligente.data.network.TokenManager
import br.com.porteirointeligente.data.network.dto.LoginRequest
import br.com.porteirointeligente.data.network.dto.RegisterRequest
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun getAuthToken() = tokenManager.getToken()

    suspend fun login(loginRequest: LoginRequest): Result<Unit> {
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                tokenManager.saveToken(response.body()!!.token)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Erro do servidor"))
            }
        } catch (e: Exception) {
            handleNetworkError(e)
        }
    }

    suspend fun register(registerRequest: RegisterRequest): Result<Unit> {
        return try {
            val response = apiService.register(registerRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Erro do servidor"))
            }
        } catch (e: Exception) {
            handleNetworkError(e)
        }
    }

    suspend fun logout() {
        tokenManager.deleteToken()
    }

    /** Traduz exceções de rede em mensagens amigáveis para o usuário */
    private fun handleNetworkError(e: Exception): Result<Unit> = when (e) {
        is ConnectException -> Result.failure(
            Exception("Servidor não disponível. Verifique se o backend está rodando em ${BuildConfig.API_BASE_URL}")
        )
        is SocketTimeoutException -> Result.failure(
            Exception("Tempo limite excedido. Servidor muito lento ou inacessível.")
        )
        is UnknownHostException -> Result.failure(
            Exception("Servidor não encontrado. Verifique a URL de conexão.")
        )
        else -> Result.failure(
            Exception(e.message ?: "Erro desconhecido ao conectar ao servidor")
        )
    }
}
