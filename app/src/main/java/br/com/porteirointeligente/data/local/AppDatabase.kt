package br.com.porteirointeligente.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.porteirointeligente.data.local.dao.OwnerDao
import br.com.porteirointeligente.data.local.dao.VisitDao
import br.com.porteirointeligente.data.local.entity.OwnerEntity
import br.com.porteirointeligente.data.local.entity.VisitEntity

/**
 * Banco de dados Room do aplicativo.
 *
 * Os dados são armazenados localmente em SQLite.
 * O Android já oferece criptografia em nível de sistema (FBE - File-Based Encryption)
 * para todos os dados no diretório do app, garantindo que estejam seguros mesmo
 * em dispositivos com bloqueio de tela.
 *
 * Para backup externo, utilize a funcionalidade "Backup para Google Drive" nos Ajustes.
 */
@Database(
    entities = [
        VisitEntity::class,
        OwnerEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun visitDao(): VisitDao
    abstract fun ownerDao(): OwnerDao

    companion object {
        const val DATABASE_NAME = "porteiro_inteligente.db"

        /**
         * Cria a instância do banco de dados.
         */
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
