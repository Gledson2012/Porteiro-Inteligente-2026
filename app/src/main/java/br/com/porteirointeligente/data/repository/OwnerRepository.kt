package br.com.porteirointeligente.data.repository

import br.com.porteirointeligente.data.local.dao.OwnerDao
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.domain.model.Owner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório de moradores (Owners).
 *
 * Gerencia a persistência e recuperação de dados de moradores.
 */
@Singleton
class OwnerRepository @Inject constructor(
    private val ownerDao: OwnerDao
) {

    fun observeAllOwners(): Flow<List<Owner>> =
        ownerDao.getAllOwners().map { entities -> entities.map { it.toDomain() } }

    suspend fun getOwnerById(id: Long): Owner? =
        ownerDao.getOwnerById(id)?.toDomain()

    suspend fun getOwnerByApartamento(apartamento: String): Owner? =
        ownerDao.getOwnerByApartamento(apartamento)?.toDomain()

    suspend fun insertOwner(owner: Owner): Long =
        ownerDao.insertOwner(OwnerEntity.fromDomain(owner))

    suspend fun updateOwner(owner: Owner) =
        ownerDao.updateOwner(OwnerEntity.fromDomain(owner))

    suspend fun deleteOwner(owner: Owner) =
        ownerDao.deleteOwner(OwnerEntity.fromDomain(owner))

    suspend fun deleteAll() = ownerDao.deleteAll()
}
