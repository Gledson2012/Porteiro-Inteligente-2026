package br.com.porteirointeligente.data.repository

import br.com.porteirointeligente.data.local.dao.OwnerDao
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.data.network.ApiService
import br.com.porteirointeligente.domain.model.Owner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnerRepository @Inject constructor(
    private val ownerDao: OwnerDao,
    private val apiService: ApiService
) {

    fun observeAllOwners(): Flow<List<Owner>> = flow {
        // Tenta sincronizar com a API primeiro
        try {
            val response = apiService.getOwners()
            if (response.isSuccessful && response.body() != null) {
                val owners = response.body()!!.map { it.toDomain() }
                ownerDao.deleteAll()
                ownerDao.insertAll(owners.map { OwnerEntity.fromDomain(it) })
            }
        } catch (e: Exception) {
            // Ignorado — usará o cache local
        }

        // Observa o banco Room reativamente para refletir mudanças locais
        emitAll(
            ownerDao.getAllOwners().map { entities ->
                entities.map { it.toDomain() }
            }
        )
    }

    suspend fun getOwnerById(id: Long): Owner? {
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

    /** Insere diretamente no banco local sem chamar a API (usado em restore de backup) */
    suspend fun insertOwnerLocal(owner: Owner) {
        ownerDao.insertOwner(OwnerEntity.fromDomain(owner))
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
