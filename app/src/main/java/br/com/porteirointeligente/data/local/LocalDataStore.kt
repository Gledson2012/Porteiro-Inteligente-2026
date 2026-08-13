package br.com.porteirointeligente.data.local

import androidx.room.withTransaction
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.data.local.entity.VisitEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Operações que precisam alterar moradores e visitas como uma única transação.
 */
@Singleton
class LocalDataStore @Inject constructor(
    private val database: AppDatabase
) {
    suspend fun clearAll() {
        database.withTransaction {
            database.visitDao().clearAll()
            database.ownerDao().deleteAll()
        }
    }

    suspend fun replaceAll(owners: List<OwnerEntity>, visits: List<VisitEntity>) {
        database.withTransaction {
            database.visitDao().clearAll()
            database.ownerDao().deleteAll()
            database.ownerDao().insertAll(owners)
            database.visitDao().insertAll(visits)
        }
    }
}
