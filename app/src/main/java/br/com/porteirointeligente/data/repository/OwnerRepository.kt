package br.com.porteirointeligente.data.repository

import br.com.porteirointeligente.data.local.dao.OwnerDao
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.data.network.ApiService
import br.com.porteirointeligente.domain.model.Owner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnerRepository @Inject constructor(
    private val ownerDao: OwnerDao,
    private val apiService: ApiService
) {

    fun observeAllOwners(): Flow<List<Owner>> = flow {
        try {
            val response = apiService.getOwners()
            if (response.isSuccessful && response.body() != null) {
                val owners = response.body()!!.map { it.toDomain() }
                // Salvar no cache local
                ownerDao.deleteAll()
                ownerDao.insertAll(owners.map { OwnerEntity.fromDomain(it) })
                emit(owners)
            } else {
                // Em caso de falha, emita do cache
                val cachedOwners = ownerDao.getAllOwnersList().map { it.toDomain() }
                emit(cachedOwners)
            }
        } catch (e: Exception) {
            // Em caso de exceção (ex: sem rede), emita do cache
            val cachedOwners = ownerDao.getAllOwnersList().map { it.toDomain() }
            emit(cachedOwners)
        }
    }

    suspend fun getOwnerById(id: Long): Owner? {
        // Busca primeiro no cache, depois na rede se necessário
        return ownerDao.getOwnerById(id)?.toDomain()
    }

    suspend fun insertOwner(owner: Owner): Result<Owner> {
        return try {
            val response = apiService.addOwner(owner.toDto())
            if (response.isSuccessful && response.body() != null) {
                val newOwner = response.body()!!
                ownerDao.insertOwner(OwnerEntity.fromDomain(newOwner))
                Result.success(newOwner)
            } else {
                Result.failure(Exception("Erro ao adicionar morador via API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOwner(owner: Owner): Result<Owner> {
        return try {
            val response = apiService.updateOwner(owner.id, owner.toDto())
            if (response.isSuccessful && response.body() != null) {
                val updatedOwner = response.body()!!
                ownerDao.updateOwner(OwnerEntity.fromDomain(updatedOwner))
                Result.success(updatedOwner)
            } else {
                Result.failure(Exception("Erro ao atualizar morador via API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteOwner(owner: Owner): Result<Unit> {
        return try {
            val response = apiService.deleteOwner(owner.id)
            if (response.isSuccessful) {
                ownerDao.deleteOwner(OwnerEntity.fromDomain(owner))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao deletar morador via API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAll() {
        ownerDao.deleteAll()
    }
}

