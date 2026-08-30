package br.com.porteirointeligente.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.porteirointeligente.util.KeyDerivation
import br.com.porteirointeligente.util.LocalDataCrypto
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
 * Funciona 100% offline. A senha é armazenada apenas como um hash PBKDF2
 * com salt aleatório; o DataStore guarda somente a sessão e o verificador.
 */
@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localDataCrypto: LocalDataCrypto
) {
    private val dataStore = context.dataStore

    companion object {
        private val REGISTERED_USERNAME_KEY = stringPreferencesKey("registered_username")
        private val AUTHENTICATED_USERNAME_KEY = stringPreferencesKey("authenticated_username")
        private val PASSWORD_HASH_KEY = stringPreferencesKey("password_hash")
        private val PASSWORD_SALT_KEY = stringPreferencesKey("password_salt")

        // Chaves usadas por versões antigas; são migradas após um login válido.
        private val LEGACY_USERNAME_KEY = stringPreferencesKey("local_username")
        private val LEGACY_PASSWORD_KEY = stringPreferencesKey("local_password")
    }

    /** Observa se há um usuário logado */
    fun getAuthToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[AUTHENTICATED_USERNAME_KEY]
        }
    }

    /** Login local — verifica o hash e cria uma sessão autenticada. */
    suspend fun login(username: String, password: String): Result<Unit> {
        val normalizedUsername = username.trim()
        if (normalizedUsername.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Usuário e senha são obrigatórios"))
        }

        val prefs = dataStore.data.first()
        val savedUser = prefs[REGISTERED_USERNAME_KEY] ?: prefs[LEGACY_USERNAME_KEY]
        val savedHash = prefs[PASSWORD_HASH_KEY]?.let {
            runCatching { localDataCrypto.decryptText(it) }.getOrNull()
        }
        val savedSalt = prefs[PASSWORD_SALT_KEY]?.let {
            runCatching { localDataCrypto.decryptText(it) }.getOrNull()
        }
        val legacyPassword = prefs[LEGACY_PASSWORD_KEY]

        if (savedUser == null) {
            return Result.failure(Exception("Nenhuma conta cadastrada. Crie uma conta primeiro."))
        }

        val valid = if (normalizedUsername == savedUser && savedHash != null && savedSalt != null) {
            verifyPassword(password, savedSalt, savedHash)
        } else {
            // Migração única de credenciais criadas pela versão anterior.
            normalizedUsername == savedUser && legacyPassword != null && password == legacyPassword
        }

        return if (valid) {
            dataStore.edit { editPrefs ->
                editPrefs[REGISTERED_USERNAME_KEY] = normalizedUsername
                editPrefs[AUTHENTICATED_USERNAME_KEY] = normalizedUsername

                if (savedHash != null && savedSalt != null) {
                    // Regrava verificadores legados em formato protegido após o login.
                    editPrefs[PASSWORD_SALT_KEY] = localDataCrypto.encryptText(savedSalt)
                    editPrefs[PASSWORD_HASH_KEY] = localDataCrypto.encryptText(savedHash)
                } else {
                    val salt = KeyDerivation.newSalt()
                    editPrefs[PASSWORD_SALT_KEY] = localDataCrypto.encryptText(KeyDerivation.encode(salt))
                    editPrefs[PASSWORD_HASH_KEY] = localDataCrypto.encryptText(hashPassword(password, salt))
                }

                editPrefs.remove(LEGACY_USERNAME_KEY)
                editPrefs.remove(LEGACY_PASSWORD_KEY)
            }
            Result.success(Unit)
        } else {
            Result.failure(Exception("Usuário ou senha inválidos"))
        }
    }

    /** Registro local — salva as credenciais no DataStore */
    suspend fun register(username: String, password: String): Result<Unit> {
        val normalizedUsername = username.trim()
        if (normalizedUsername.length !in 3..64) {
            return Result.failure(Exception("O usuário deve ter entre 3 e 64 caracteres"))
        }
        if (password.length < 8) {
            return Result.failure(Exception("A senha deve ter no mínimo 8 caracteres"))
        }
        if (password.length > 128) {
            return Result.failure(Exception("A senha deve ter no máximo 128 caracteres"))
        }

        val prefs = dataStore.data.first()
        if (prefs[REGISTERED_USERNAME_KEY] != null || prefs[LEGACY_USERNAME_KEY] != null) {
            return Result.failure(Exception("Já existe uma conta neste dispositivo"))
        }

        val salt = KeyDerivation.newSalt()
        dataStore.edit { prefs ->
            prefs[REGISTERED_USERNAME_KEY] = normalizedUsername
            prefs[PASSWORD_SALT_KEY] = localDataCrypto.encryptText(KeyDerivation.encode(salt))
            prefs[PASSWORD_HASH_KEY] = localDataCrypto.encryptText(hashPassword(password, salt))
            prefs.remove(AUTHENTICATED_USERNAME_KEY)
        }
        return Result.success(Unit)
    }

    /** Logout — remove os dados de autenticação */
    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(AUTHENTICATED_USERNAME_KEY)
        }
    }

    /** Remove a conta local e encerra a sessão para atender à exclusão de dados do usuário. */
    suspend fun deleteAccount() {
        dataStore.edit { prefs ->
            prefs.remove(REGISTERED_USERNAME_KEY)
            prefs.remove(AUTHENTICATED_USERNAME_KEY)
            prefs.remove(PASSWORD_HASH_KEY)
            prefs.remove(PASSWORD_SALT_KEY)
            prefs.remove(LEGACY_USERNAME_KEY)
            prefs.remove(LEGACY_PASSWORD_KEY)
        }
    }

    private fun hashPassword(password: String, salt: ByteArray): String =
        KeyDerivation.encode(KeyDerivation.derive(password.toCharArray(), salt))

    private fun verifyPassword(password: String, encodedSalt: String, encodedHash: String): Boolean {
        return try {
            val salt = KeyDerivation.decode(encodedSalt)
            val expectedHash = KeyDerivation.decode(encodedHash)
            val actualHash = KeyDerivation.derive(password.toCharArray(), salt)
            KeyDerivation.securelyEquals(actualHash, expectedHash)
        } catch (_: Exception) {
            false
        }
    }
}
