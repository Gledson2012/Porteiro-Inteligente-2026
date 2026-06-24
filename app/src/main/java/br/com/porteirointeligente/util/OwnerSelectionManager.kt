package br.com.porteirointeligente.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.selectionDataStore by preferencesDataStore(name = "owner_selection")

/**
 * Gerencia o morador atualmente selecionado no app.
 *
 * Permite que múltiplos moradores sejam cadastrados e o usuário
 * alterne entre eles. O ID do morador selecionado é persistido
 * em DataStore e restaurado entre sessões.
 */
@Singleton
class OwnerSelectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val SELECTED_OWNER_ID = longPreferencesKey("selected_owner_id")

    /**
     * Flow com o ID do morador atualmente selecionado.
     * Retorna null se nenhum morador foi selecionado ainda.
     */
    val selectedOwnerId: Flow<Long?> = context.selectionDataStore.data
        .map { preferences ->
            val id = preferences[SELECTED_OWNER_ID]
            if (id != null && id > 0L) id else null
        }

    /**
     * Obtém o ID do morador selecionado de forma síncrona (suspensa).
     */
    suspend fun getSelectedOwnerId(): Long? {
        return selectedOwnerId.first()
    }

    /**
     * Define o morador atualmente selecionado.
     */
    suspend fun selectOwner(ownerId: Long) {
        context.selectionDataStore.edit { preferences ->
            preferences[SELECTED_OWNER_ID] = ownerId
        }
    }

    /**
     * Limpa a seleção (quando o morador é excluído, etc.).
     */
    suspend fun clearSelection() {
        context.selectionDataStore.edit { preferences ->
            preferences.remove(SELECTED_OWNER_ID)
        }
    }
}
