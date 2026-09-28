package br.com.porteirointeligente.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AppTheme { LIGHT, DARK, SYSTEM }

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_KEY = stringPreferencesKey("theme_preference")
    private val DYNAMIC_COLOR_KEY = stringPreferencesKey("dynamic_color_preference")
    private val FLAG_SECURE_KEY = stringPreferencesKey("flag_secure_preference")
    private val BIOMETRIC_LOGIN_KEY = stringPreferencesKey("biometric_login_preference")

    val themeFlow: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
            runCatching { AppTheme.valueOf(themeName) }
                .getOrDefault(AppTheme.SYSTEM)
        }

    val dynamicColorFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DYNAMIC_COLOR_KEY] ?: "false"
        }
        .map { it.toBoolean() }

    val flagSecureFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FLAG_SECURE_KEY] ?: "false"
        }
        .map { it.toBoolean() }

    val biometricLoginFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[BIOMETRIC_LOGIN_KEY] ?: "true" // Padrão ativado se disponível
        }
        .map { it.toBoolean() }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled.toString()
        }
    }

    suspend fun setFlagSecure(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FLAG_SECURE_KEY] = enabled.toString()
        }
    }

    suspend fun setBiometricLogin(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_LOGIN_KEY] = enabled.toString()
        }
    }
}
