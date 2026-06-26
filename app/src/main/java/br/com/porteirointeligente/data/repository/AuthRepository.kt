package br.com.porteirointeligente.data.repository

import br.com.porteirointeligente.data.network.ApiService
import br.com.porteirointeligente.data.network.TokenManager
import br.com.porteirointeligente.data.network.dto.LoginRequest
import br.com.porteirointeligente.data.network.dto.RegisterRequest
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
                Result.failure(Exception(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(registerRequest: RegisterRequest): Result<Unit> {
        return try {
            val response = apiService.register(registerRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.deleteToken()
    }
}
