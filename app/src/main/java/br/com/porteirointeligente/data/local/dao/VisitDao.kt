package br.com.porteirointeligente.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.porteirointeligente.data.local.entity.VisitEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para a entidade [VisitEntity].
 *
 * Define as operações de leitura e escrita no banco de dados Room
 * relacionadas a visitas.
 */
@Dao
interface VisitDao {

    @Query("SELECT * FROM visits ORDER BY dataEntrada DESC")
    fun getAllVisits(): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE id = :id LIMIT 1")
    suspend fun getVisitById(id: Long): VisitEntity?

    @Query("SELECT * FROM visits WHERE status = :status ORDER BY dataEntrada DESC")
    fun getVisitsByStatus(status: br.com.porteirointeligente.domain.model.VisitStatus): Flow<List<VisitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitEntity): Long

    @Update
    suspend fun updateVisit(visit: VisitEntity)

    @Delete
    suspend fun deleteVisit(visit: VisitEntity)

    @Query("DELETE FROM visits")
    suspend fun clearAll()
}
