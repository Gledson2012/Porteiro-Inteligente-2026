package br.com.porteirointeligente.data.repository

import br.com.porteirointeligente.data.local.dao.OwnerDao
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.util.LocalDataCrypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositório de moradores — 100% offline.
 *
 * Todas as operações são feitas diretamente no Room (SQLite).
 * Sem dependência de API REST ou Firebase.
 */
@Singleton
class OwnerRepository @Inject constructor(
    private val ownerDao: OwnerDao,
    private val localDataCrypto: LocalDataCrypto
) {

    /** Observa todos os moradores do banco local */
    fun observeAllOwners(): Flow<List<Owner>> =
        ownerDao.getAllOwners().map { entities ->
            entities
                .map(localDataCrypto::decryptOwner)
                .map { it.toDomain() }
                .sortedBy { it.nome.lowercase() }
        }

    /** Busca um morador pelo ID */
    suspend fun getOwnerById(id: Long): Owner? =
        ownerDao.getOwnerById(id)?.let(localDataCrypto::decryptOwner)?.toDomain()

    /** Insere um novo morador no banco local */
    suspend fun insertOwner(owner: Owner): Owner {
        val entity = localDataCrypto.encryptOwner(OwnerEntity.fromDomain(owner))
        val id = ownerDao.insertOwner(entity)
        return owner.copy(id = id)
    }

    /** Atualiza um morador existente */
    suspend fun updateOwner(owner: Owner) {
        ownerDao.updateOwner(localDataCrypto.encryptOwner(OwnerEntity.fromDomain(owner)))
    }

    /** Deleta um morador */
    suspend fun deleteOwner(owner: Owner) {
        ownerDao.deleteOwnerById(owner.id)
    }

    /** Deleta todos os moradores */
    suspend fun deleteAll() {
        ownerDao.deleteAll()
    }
}
