package br.com.porteirointeligente.data.repository

import br.com.porteirointeligente.data.local.dao.VisitDao
import br.com.porteirointeligente.data.local.entity.VisitEntity
import br.com.porteirointeligente.data.network.ApiService
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitRepository @Inject constructor(
    private val visitDao: VisitDao,
    private val apiService: ApiService
) {

    fun observeAllVisits(): Flow<List<Visit>> = flow {
        try {
            val response = apiService.getVisits()
            if (response.isSuccessful && response.body() != null) {
                val visits = response.body()!!.map { it.toDomain() }
                visitDao.clearAll()
                visitDao.insertAll(visits.map { VisitEntity.fromDomain(it) })
                emit(visits)
            } else {
                emit(visitDao.getAllVisitsList().map { it.toDomain() })
            }
        } catch (e: Exception) {
            emit(visitDao.getAllVisitsList().map { it.toDomain() })
        }
    }

    fun observeVisitsByStatus(status: VisitStatus): Flow<List<Visit>> = flow {
        try {
            val response = apiService.getVisits()
            if (response.isSuccessful && response.body() != null) {
                val visits = response.body()!!
                    .map { it.toDomain() }
                    .filter { it.status == status }
                emit(visits)
            } else {
                emit(visitDao.getVisitsByStatusList(status).map { it.toDomain() })
            }
        } catch (e: Exception) {
            emit(visitDao.getVisitsByStatusList(status).map { it.toDomain() })
        }
    }


    suspend fun getVisitById(id: Long): Visit? =
        visitDao.getVisitById(id)?.toDomain()

    suspend fun insertVisit(visit: Visit): Result<Visit> {
        return try {
            val response = apiService.addVisit(visit.toDto())
            if (response.isSuccessful && response.body() != null) {
                val newVisit = response.body()!!.toDomain()
                visitDao.insertVisit(VisitEntity.fromDomain(newVisit))
                Result.success(newVisit)
            } else {
                Result.failure(Exception("Erro ao adicionar visita via API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVisit(visit: Visit): Result<Visit> {
        return try {
            val response = apiService.updateVisit(visit.id, visit.toDto())
            if (response.isSuccessful && response.body() != null) {
                val updatedVisit = response.body()!!.toDomain()
                visitDao.updateVisit(VisitEntity.fromDomain(updatedVisit))
                Result.success(updatedVisit)
            } else {
                Result.failure(Exception("Erro ao atualizar visita via API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAll() = visitDao.clearAll()
}

