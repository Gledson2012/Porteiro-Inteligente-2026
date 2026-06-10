package br.com.porteirointeligente.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.porteirointeligente.data.local.dao.OwnerDao
import br.com.porteirointeligente.data.local.dao.VisitDao
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.data.local.entity.VisitEntity

/**
 * Banco de dados Room do aplicativo.
 *
 * Centraliza o acesso às DAOs e mantém a configuração de migrações
 * e entidades persistidas.
 */
@Database(
    entities = [
        VisitEntity::class,
        OwnerEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun visitDao(): VisitDao
    abstract fun ownerDao(): OwnerDao

    companion object {
        const val DATABASE_NAME = "porteiro_inteligente.db"
    }
}
