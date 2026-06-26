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
 * Repositório de visitas — 100% offline.
 *
 * Todas as operações são feitas diretamente no Room (SQLite).
 * Sem dependência de API REST ou Firebase.
 */
@Singleton
class VisitRepository @Inject constructor(
    private val visitDao: VisitDao
) {

    /** Observa todas as visitas do banco local */
    fun observeAllVisits(): Flow<List<Visit>> =
        visitDao.getAllVisits().map { entities ->
            entities.map { it.toDomain() }
        }

    /** Observa visitas filtradas por status */
    fun observeVisitsByStatus(status: VisitStatus): Flow<List<Visit>> =
        visitDao.getAllVisits().map { entities ->
            entities
                .map { it.toDomain() }
                .filter { it.status == status }
        }

    /** Busca uma visita pelo ID */
    suspend fun getVisitById(id: Long): Visit? =
        visitDao.getVisitById(id)?.toDomain()

    /** Insere uma nova visita no banco local */
    suspend fun insertVisit(visit: Visit): Visit {
        val entity = VisitEntity.fromDomain(visit)
        val id = visitDao.insertVisit(entity)
        return visit.copy(id = id)
    }

    /** Atualiza uma visita existente */
    suspend fun updateVisit(visit: Visit) {
        visitDao.updateVisit(VisitEntity.fromDomain(visit))
    }

    /** Remove todas as visitas */
    suspend fun clearAll() = visitDao.clearAll()
}
