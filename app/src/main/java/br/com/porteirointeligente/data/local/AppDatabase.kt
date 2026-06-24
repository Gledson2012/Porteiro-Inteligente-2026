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
 *
 * Para novas migrações:
 * 1. Altere a [version] para o próximo número
 * 2. Adicione um objeto [androidx.room.migration.Migration] em [MIGRATIONS]
 * 3. Execute o app para gerar o schema no diretório de exportação
 *
 * @see AppDatabase.Companion.MIGRATIONS
 */
@Database(
    entities = [
        VisitEntity::class,
        OwnerEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun visitDao(): VisitDao
    abstract fun ownerDao(): OwnerDao

    companion object {
        const val DATABASE_NAME = "porteiro_inteligente.db"

        /**
         * Lista de migrações explícitas do banco de dados.
         *
         * Para adicionar uma nova migração (ex: version 6 → 7):
         * ```
         * val MIGRATION_6_7 = object : Migration(6, 7) {
         *     override fun migrate(db: SupportSQLiteDatabase) {
         *         db.execSQL("ALTER TABLE visits ADD COLUMN observacao TEXT NOT NULL DEFAULT ''")
         *     }
         * }
         * ```
         *
         * E adicione-a a esta lista.
         */
        val MIGRATIONS: Array<androidx.room.migration.Migration> = arrayOf(
            // Migrações futuras serão adicionadas aqui
            // Exemplo: MIGRATION_6_7
        )
    }
}
