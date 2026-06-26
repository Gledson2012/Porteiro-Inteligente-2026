package br.com.porteirointeligente.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

/**
 * Repositório de autenticação local.
 *
 * Agora funciona 100% offline — armazena o nome de usuário logado no DataStore.
 * O registro cria uma conta local, e o login verifica as credenciais.
 * Não depende mais de backend REST ou Firebase.
 */
@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val USERNAME_KEY = stringPreferencesKey("local_username")
        private val PASSWORD_KEY = stringPreferencesKey("local_password")
    }

    /** Observa se há um usuário logado */
    fun getAuthToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }
    }

    /** Login local — verifica se as credenciais batem com o registro */
    suspend fun login(username: String, password: String): Result<Unit> {
        val prefs = dataStore.data.first()
        val savedUser = prefs[USERNAME_KEY]
        val savedPass = prefs[PASSWORD_KEY]

        if (savedUser == null || savedPass == null) {
            // Primeiro login: cria conta automaticamente
            dataStore.edit { editPrefs ->
                editPrefs[USERNAME_KEY] = username
                editPrefs[PASSWORD_KEY] = password
            }
            return Result.success(Unit)
        }

        return if (username == savedUser && password == savedPass) {
            dataStore.edit { editPrefs ->
                editPrefs[USERNAME_KEY] = username
            }
            Result.success(Unit)
        } else {
            Result.failure(Exception("Usuário ou senha inválidos"))
        }
    }

    /** Registro local — salva as credenciais no DataStore */
    suspend fun register(username: String, password: String): Result<Unit> {
        if (username.isBlank()) {
            return Result.failure(Exception("Usuário não pode estar em branco"))
        }
        if (password.length < 4) {
            return Result.failure(Exception("A senha deve ter no mínimo 4 caracteres"))
        }

        dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username
            prefs[PASSWORD_KEY] = password
        }
        return Result.success(Unit)
    }

    /** Logout — remove os dados de autenticação */
    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(USERNAME_KEY)
        }
    }
}
