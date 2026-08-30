package br.com.porteirointeligente.data.repository

import br.com.porteirointeligente.data.local.dao.VisitDao
import br.com.porteirointeligente.data.local.entity.VisitEntity
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import br.com.porteirointeligente.util.LocalDataCrypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório de visitas — 100% offline.
 *
 * Todas as operações são feitas diretamente no Room (SQLite).
 * Sem dependência de API REST ou Firebase.
 */
@Singleton
class VisitRepository @Inject constructor(
    private val visitDao: VisitDao,
    private val localDataCrypto: LocalDataCrypto
) {

    /** Observa todas as visitas do banco local */
    fun observeAllVisits(): Flow<List<Visit>> =
        visitDao.getAllVisits().map { entities ->
            entities
                .map(localDataCrypto::decryptVisit)
                .map { it.toDomain() }
                .sortedByDescending { it.dataEntrada }
        }

    /** Observa visitas filtradas por status */
    fun observeVisitsByStatus(status: VisitStatus): Flow<List<Visit>> =
        visitDao.getAllVisits().map { entities ->
            entities
                .map(localDataCrypto::decryptVisit)
                .map { it.toDomain() }
                .filter { it.status == status }
                .sortedByDescending { it.dataEntrada }
        }

    /** Busca uma visita pelo ID */
    suspend fun getVisitById(id: Long): Visit? =
        visitDao.getVisitById(id)?.let(localDataCrypto::decryptVisit)?.toDomain()

    /** Insere uma nova visita no banco local */
    suspend fun insertVisit(visit: Visit): Visit {
        val entity = localDataCrypto.encryptVisit(VisitEntity.fromDomain(visit))
        val id = visitDao.insertVisit(entity)
        return visit.copy(id = id)
    }

    /** Atualiza uma visita existente */
    suspend fun updateVisit(visit: Visit) {
        visitDao.updateVisit(localDataCrypto.encryptVisit(VisitEntity.fromDomain(visit)))
    }

    /** Remove todas as visitas */
    suspend fun clearAll() = visitDao.clearAll()

    /** Deleta uma visita específica */
    suspend fun deleteVisit(visit: Visit) {
        visitDao.deleteVisitById(visit.id)
    }
}
