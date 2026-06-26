package br.com.porteirointeligente.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para a entidade [OwnerEntity].
 */
@Dao
interface OwnerDao {

    @Query("SELECT * FROM owners ORDER BY nome ASC")
    fun getAllOwners(): Flow<List<OwnerEntity>>

    @Query("SELECT * FROM owners WHERE id = :id LIMIT 1")
    suspend fun getOwnerById(id: Long): OwnerEntity?

    @Query("SELECT * FROM owners ORDER BY nome ASC")
    fun getAllOwnersList(): List<OwnerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(owners: List<OwnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwner(owner: OwnerEntity): Long

    @Update
    suspend fun updateOwner(owner: OwnerEntity)

    @Delete
    suspend fun deleteOwner(owner: OwnerEntity)

    @Query("DELETE FROM owners")
    suspend fun deleteAll()
}
