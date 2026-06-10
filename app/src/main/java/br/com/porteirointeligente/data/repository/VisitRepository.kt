package br.com.porteirointeligente.data.repository

import br.com.porteirointeligente.data.local.dao.VisitDao
import br.com.porteirointeligente.data.local.entity.VisitEntity
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório de visitas.
 *
 * Faz a mediação entre a camada de domínio ([Visit]) e a camada
 * de dados ([VisitEntity] / [VisitDao]), aplicando as conversões
 * necessárias.
 */
@Singleton
class VisitRepository @Inject constructor(
    private val visitDao: VisitDao
) {

    fun observeAllVisits(): Flow<List<Visit>> =
        visitDao.getAllVisits().map { entities -> entities.map { it.toDomain() } }

    fun observeVisitsByStatus(status: VisitStatus): Flow<List<Visit>> =
        visitDao.getVisitsByStatus(status).map { entities -> entities.map { it.toDomain() } }

    suspend fun getVisitById(id: Long): Visit? =
        visitDao.getVisitById(id)?.toDomain()

    suspend fun insertVisit(visit: Visit): Long =
        visitDao.insertVisit(VisitEntity.fromDomain(visit))

    suspend fun updateVisit(visit: Visit) =
        visitDao.updateVisit(VisitEntity.fromDomain(visit))

    suspend fun deleteVisit(visit: Visit) =
        visitDao.deleteVisit(VisitEntity.fromDomain(visit))

    suspend fun clearAll() = visitDao.clearAll()
}
