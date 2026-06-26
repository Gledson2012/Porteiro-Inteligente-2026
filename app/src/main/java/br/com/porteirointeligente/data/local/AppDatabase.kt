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
 * ⚠️  NOTA SOBRE MIGRAÇÕES
 * O AppModule está configurado com fallbackToDestructiveMigration(),
 * o que significa que, se não houver uma migração explícita para a
 * nova versão, o banco será recriado do zero (perdendo dados).
 *
 * Para adicionar migrações explícitas:
 * 1. Altere a [version] para o próximo número
 * 2. Crie um Migration object em [MIGRATIONS]
 * 3. Execute o app para gerar o schema no diretório de exportação
 * 4. REMOVA a dependência do fallbackToDestructiveMigration()
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
         * Após criar a migração:
         * 1. Adicione-a na lista abaixo
         * 2. Remova o .fallbackToDestructiveMigration() do AppModule
         * 3. Teste a migração com dados reais antes de publicar
         */
        val MIGRATIONS: Array<androidx.room.migration.Migration> = arrayOf(
            // Migrações futuras serão adicionadas aqui
            // Exemplo:
            // MIGRATION_6_7
        )
    }
}
