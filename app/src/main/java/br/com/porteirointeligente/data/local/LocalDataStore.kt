package br.com.porteirointeligente.data.local

import androidx.room.withTransaction
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.data.local.entity.VisitEntity
import br.com.porteirointeligente.util.LocalDataCrypto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Operações que precisam alterar moradores e visitas como uma única transação.
 */
@Singleton
class LocalDataStore @Inject constructor(
    private val database: AppDatabase,
    private val localDataCrypto: LocalDataCrypto
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
            database.ownerDao().insertAll(owners.map(localDataCrypto::encryptOwner))
            database.visitDao().insertAll(visits.map(localDataCrypto::encryptVisit))
        }
    }
}
