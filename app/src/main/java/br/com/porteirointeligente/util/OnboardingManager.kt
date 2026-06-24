package br.com.porteirointeligente.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding")

/**
 * Gerencia se o onboarding/tour guiado já foi exibido para o usuário.
 */
@Singleton
class OnboardingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

    /** Flow que indica se o onboarding já foi concluído */
    val isOnboardingCompleted: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    /** Verifica de forma síncrona se o onboarding deve ser exibido */
    suspend fun shouldShowOnboarding(): Boolean {
        return !isOnboardingCompleted.first()
    }

    /** Marca o onboarding como concluído */
    suspend fun completeOnboarding() {
        context.onboardingDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    /** Permite reiniciar o onboarding (para testes) */
    suspend fun resetOnboarding() {
        context.onboardingDataStore.edit { preferences ->
            preferences.remove(ONBOARDING_COMPLETED)
        }
    }
}
